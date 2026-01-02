package com.dramallama.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dramallama.app.data.database.MeetingNote
import com.dramallama.app.data.database.TeamMember
import com.dramallama.app.data.repository.TeamRepository
import com.dramallama.app.data.repository.toLocalDateTime
import com.dramallama.app.ui.components.TrendDataPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MemberDetailViewModel(
    private val repository: TeamRepository,
    private val memberId: Long
) : ViewModel() {
    
    val member: StateFlow<TeamMember?> = repository.getMemberByIdFlow(memberId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val notes: StateFlow<List<MeetingNote>> = repository.getNotesForMemberFlow(memberId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Mood trend data: every note with mood -> data point
    val moodTrend: StateFlow<List<TrendDataPoint>> = repository.getNotesForMemberFlow(memberId)
        .map { notesList ->
            notesList.filter { it.mood != null }
                .map { note ->
                    TrendDataPoint(
                        date = note.timestampEpochSecond.toLocalDateTime().toLocalDate(),
                        value = note.mood!!
                    )
                }
                .sortedBy { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Productivity trend data: every note with productivity -> data point
    val productivityTrend: StateFlow<List<TrendDataPoint>> = repository.getNotesForMemberFlow(memberId)
        .map { notesList ->
            notesList.filter { it.productivity != null }
                .map { note ->
                    TrendDataPoint(
                        date = note.timestampEpochSecond.toLocalDateTime().toLocalDate(),
                        value = note.productivity!!
                    )
                }
                .sortedBy { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Total meeting count
    val totalMeetings: StateFlow<Int> = repository.getNotesForMemberFlow(memberId)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    private val _showNoteSheet = MutableStateFlow(false)
    val showNoteSheet: StateFlow<Boolean> = _showNoteSheet.asStateFlow()
    
    // Track if we're editing an existing note (null = adding new)
    private val _editingNote = MutableStateFlow<MeetingNote?>(null)
    val editingNote: StateFlow<MeetingNote?> = _editingNote.asStateFlow()
    
    private val _noteContent = MutableStateFlow("")
    val noteContent: StateFlow<String> = _noteContent.asStateFlow()
    
    private val _noteDate = MutableStateFlow(LocalDate.now())
    val noteDate: StateFlow<LocalDate> = _noteDate.asStateFlow()
    
    private val _noteTime = MutableStateFlow(LocalTime.now())
    val noteTime: StateFlow<LocalTime> = _noteTime.asStateFlow()
    
    // Sentiment tracking state
    private val _noteMood = MutableStateFlow<Int?>(null)
    val noteMood: StateFlow<Int?> = _noteMood.asStateFlow()
    
    private val _noteProductivity = MutableStateFlow<Int?>(null)
    val noteProductivity: StateFlow<Int?> = _noteProductivity.asStateFlow()
    
    private val _noteFlightRisk = MutableStateFlow<Int?>(null)
    val noteFlightRisk: StateFlow<Int?> = _noteFlightRisk.asStateFlow()
    
    fun showAddNoteSheet() {
        // Reset to current date/time for new note
        _editingNote.value = null
        _noteContent.value = ""
        _noteDate.value = LocalDate.now()
        _noteTime.value = LocalTime.now()
        _noteMood.value = null
        _noteProductivity.value = null
        _noteFlightRisk.value = null
        _showNoteSheet.value = true
    }
    
    fun showEditNoteSheet(note: MeetingNote) {
        // Populate form with existing note data
        _editingNote.value = note
        _noteContent.value = note.content
        val noteDateTime = note.timestampEpochSecond.toLocalDateTime()
        _noteDate.value = noteDateTime.toLocalDate()
        _noteTime.value = noteDateTime.toLocalTime()
        _noteMood.value = note.mood
        _noteProductivity.value = note.productivity
        _noteFlightRisk.value = note.flightRisk
        _showNoteSheet.value = true
    }
    
    fun hideNoteSheet() {
        _showNoteSheet.value = false
        _editingNote.value = null
        _noteContent.value = ""
        _noteMood.value = null
        _noteProductivity.value = null
        _noteFlightRisk.value = null
    }
    
    fun updateNoteContent(content: String) {
        _noteContent.value = content
    }
    
    fun updateNoteDate(date: LocalDate) {
        _noteDate.value = date
    }
    
    fun updateNoteTime(time: LocalTime) {
        _noteTime.value = time
    }
    
    fun updateNoteMood(mood: Int?) {
        _noteMood.value = mood
    }
    
    fun updateNoteProductivity(productivity: Int?) {
        _noteProductivity.value = productivity
    }
    
    fun updateNoteFlightRisk(flightRisk: Int?) {
        _noteFlightRisk.value = flightRisk
    }
    
    fun saveNote() {
        val content = _noteContent.value.trim()
        if (content.isNotEmpty()) {
            val timestamp = LocalDateTime.of(_noteDate.value, _noteTime.value)
            viewModelScope.launch {
                val existingNote = _editingNote.value
                if (existingNote != null) {
                    // Update existing note
                    val updatedNote = existingNote.copy(
                        content = content,
                        timestampEpochSecond = timestamp.atZone(java.time.ZoneId.systemDefault()).toEpochSecond(),
                        mood = _noteMood.value,
                        productivity = _noteProductivity.value,
                        flightRisk = _noteFlightRisk.value
                    )
                    repository.updateNote(updatedNote)
                } else {
                    // Add new note
                    repository.addMeetingNote(
                        memberId = memberId,
                        content = content,
                        timestamp = timestamp,
                        mood = _noteMood.value,
                        productivity = _noteProductivity.value,
                        flightRisk = _noteFlightRisk.value
                    )
                }
                hideNoteSheet()
            }
        }
    }
    
    fun deleteNote(note: MeetingNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun updateMember(member: TeamMember) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }
    
    class Factory(
        private val repository: TeamRepository,
        private val memberId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemberDetailViewModel(repository, memberId) as T
        }
    }
}

