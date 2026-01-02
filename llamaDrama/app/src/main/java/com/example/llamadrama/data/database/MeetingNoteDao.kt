package com.example.llamadrama.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingNoteDao {
    
    @Query("SELECT * FROM meeting_notes WHERE memberId = :memberId ORDER BY timestampEpochSecond DESC")
    fun getNotesForMemberFlow(memberId: Long): Flow<List<MeetingNote>>
    
    @Query("SELECT * FROM meeting_notes WHERE memberId = :memberId ORDER BY timestampEpochSecond DESC")
    suspend fun getNotesForMember(memberId: Long): List<MeetingNote>
    
    @Query("SELECT * FROM meeting_notes ORDER BY timestampEpochSecond DESC")
    suspend fun getAllNotes(): List<MeetingNote>
    
    @Query("SELECT * FROM meeting_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): MeetingNote?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: MeetingNote): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<MeetingNote>)
    
    @Update
    suspend fun update(note: MeetingNote)
    
    @Delete
    suspend fun delete(note: MeetingNote)
    
    @Query("DELETE FROM meeting_notes")
    suspend fun deleteAll()
}

