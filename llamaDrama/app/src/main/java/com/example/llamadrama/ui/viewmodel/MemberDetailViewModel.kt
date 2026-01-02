package com.example.llamadrama.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.llamadrama.data.database.MeetingNote
import com.example.llamadrama.data.database.TeamMember
import com.example.llamadrama.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemberDetailViewModel(
    private val repository: TeamRepository,
    private val memberId: Long
) : ViewModel() {
    
    val member: StateFlow<TeamMember?> = repository.getMemberByIdFlow(memberId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val notes: StateFlow<List<MeetingNote>> = repository.getNotesForMemberFlow(memberId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _showAddNoteSheet = MutableStateFlow(false)
    val showAddNoteSheet: StateFlow<Boolean> = _showAddNoteSheet.asStateFlow()
    
    private val _noteContent = MutableStateFlow("")
    val noteContent: StateFlow<String> = _noteContent.asStateFlow()
    
    fun showAddNoteSheet() {
        _showAddNoteSheet.value = true
    }
    
    fun hideAddNoteSheet() {
        _showAddNoteSheet.value = false
        _noteContent.value = ""
    }
    
    fun updateNoteContent(content: String) {
        _noteContent.value = content
    }
    
    fun addMeetingNote() {
        val content = _noteContent.value.trim()
        if (content.isNotEmpty()) {
            viewModelScope.launch {
                repository.addMeetingNote(memberId, content)
                hideAddNoteSheet()
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

