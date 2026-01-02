package com.dramallama.app.data.repository

import com.dramallama.app.data.database.AppDatabase
import com.dramallama.app.data.database.MeetingNote
import com.dramallama.app.data.database.TeamMember
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class TeamRepository(private val database: AppDatabase) {
    
    private val teamMemberDao = database.teamMemberDao()
    private val meetingNoteDao = database.meetingNoteDao()
    
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }
    
    // Team Members
    fun getAllMembersFlow(): Flow<List<TeamMember>> = teamMemberDao.getAllMembersFlow()
    
    fun getMemberByIdFlow(id: Long): Flow<TeamMember?> = teamMemberDao.getMemberByIdFlow(id)
    
    suspend fun getMemberById(id: Long): TeamMember? = teamMemberDao.getMemberById(id)
    
    suspend fun addMember(name: String): Long {
        return teamMemberDao.insert(TeamMember(name = name))
    }
    
    suspend fun updateMember(member: TeamMember) {
        teamMemberDao.update(member)
    }
    
    suspend fun deleteMember(member: TeamMember) {
        teamMemberDao.delete(member)
    }
    
    // Meeting Notes
    fun getNotesForMemberFlow(memberId: Long): Flow<List<MeetingNote>> = 
        meetingNoteDao.getNotesForMemberFlow(memberId)
    
    suspend fun addMeetingNote(
        memberId: Long, 
        content: String,
        timestamp: LocalDateTime = LocalDateTime.now(),
        mood: Int? = null,
        productivity: Int? = null,
        flightRisk: Int? = null
    ): Long {
        val note = MeetingNote(
            memberId = memberId,
            timestampEpochSecond = timestamp.toEpochSecond(ZoneOffset.UTC),
            content = content,
            mood = mood,
            productivity = productivity,
            flightRisk = flightRisk
        )
        val noteId = meetingNoteDao.insert(note)
        
        // Only update member's last contact if this note is more recent
        val member = teamMemberDao.getMemberById(memberId)
        member?.let {
            val noteDate = timestamp.toLocalDate().toEpochDay()
            val currentLastContact = it.lastContactEpochDay
            
            // Update only if this note is newer than current last contact (or no last contact set)
            if (currentLastContact == null || noteDate > currentLastContact) {
                teamMemberDao.update(
                    it.copy(
                        lastContactEpochDay = noteDate,
                        lastTopic = content.lines().firstOrNull()?.take(100) ?: "Meeting"
                    )
                )
            }
        }
        
        return noteId
    }
    
    suspend fun updateNote(note: MeetingNote) {
        meetingNoteDao.update(note)
        // Recalculate member's last contact after update
        recalculateMemberLastContact(note.memberId)
    }
    
    suspend fun deleteNote(note: MeetingNote) {
        val memberId = note.memberId
        meetingNoteDao.delete(note)
        // Recalculate member's last contact after deletion
        recalculateMemberLastContact(memberId)
    }
    
    /**
     * Recalculates a member's lastContactEpochDay and lastTopic based on their remaining notes.
     * Called after a note is deleted or updated.
     */
    private suspend fun recalculateMemberLastContact(memberId: Long) {
        val member = teamMemberDao.getMemberById(memberId) ?: return
        val remainingNotes = meetingNoteDao.getNotesForMember(memberId)
        
        if (remainingNotes.isEmpty()) {
            // No notes left - clear last contact info
            teamMemberDao.update(
                member.copy(
                    lastContactEpochDay = null,
                    lastTopic = null
                )
            )
        } else {
            // Find the most recent note (notes are already sorted DESC by timestamp)
            val mostRecentNote = remainingNotes.first()
            val mostRecentDate = mostRecentNote.timestampEpochSecond.toLocalDateTime().toLocalDate().toEpochDay()
            val mostRecentTopic = mostRecentNote.content.lines().firstOrNull()?.take(100) ?: "Meeting"
            
            teamMemberDao.update(
                member.copy(
                    lastContactEpochDay = mostRecentDate,
                    lastTopic = mostRecentTopic
                )
            )
        }
    }
    
    // Export/Import
    suspend fun exportToJson(): String {
        val members = teamMemberDao.getAllMembers()
        val notes = meetingNoteDao.getAllNotes()
        val exportData = ExportData(
            version = 2,
            exportedAt = LocalDateTime.now().toString(),
            members = members,
            notes = notes
        )
        return json.encodeToString(exportData)
    }
    
    suspend fun importFromJson(jsonString: String): ImportResult {
        return try {
            val importData = json.decodeFromString<ExportData>(jsonString)
            
            // Clear existing data
            meetingNoteDao.deleteAll()
            teamMemberDao.deleteAll()
            
            // Import members first (to get new IDs)
            val oldToNewMemberIds = mutableMapOf<Long, Long>()
            importData.members.forEach { member ->
                val newId = teamMemberDao.insert(member.copy(id = 0))
                oldToNewMemberIds[member.id] = newId
            }
            
            // Import notes with updated member IDs
            importData.notes.forEach { note ->
                val newMemberId = oldToNewMemberIds[note.memberId]
                if (newMemberId != null) {
                    meetingNoteDao.insert(note.copy(id = 0, memberId = newMemberId))
                }
            }
            
            ImportResult.Success(
                membersImported = importData.members.size,
                notesImported = importData.notes.size
            )
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error during import")
        }
    }
    
    @Serializable
    data class ExportData(
        val version: Int,
        val exportedAt: String,
        val members: List<TeamMember>,
        val notes: List<MeetingNote>
    )
    
    sealed class ImportResult {
        data class Success(val membersImported: Int, val notesImported: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}

// Extension functions for date conversion
fun LocalDate.toEpochDay(): Long = this.toEpochDay()
fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)
fun LocalDateTime.toEpochSecond(): Long = this.toEpochSecond(ZoneOffset.UTC)
fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC)

