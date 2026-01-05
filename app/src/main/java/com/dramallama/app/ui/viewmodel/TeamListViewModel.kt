package com.dramallama.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dramallama.app.data.database.TeamMember
import com.dramallama.app.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamListViewModel(private val repository: TeamRepository) : ViewModel() {
    
    private val allMembers = repository.getAllMembersFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // True if there are any members at all (regardless of search filter)
    val hasAnyMembers: StateFlow<Boolean> = allMembers
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val members: StateFlow<List<TeamMember>> = combine(allMembers, _searchQuery) { members, query ->
        if (query.isBlank()) {
            members
        } else {
            members.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _showAddMemberDialog = MutableStateFlow(false)
    val showAddMemberDialog: StateFlow<Boolean> = _showAddMemberDialog.asStateFlow()
    
    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun showAddDialog() {
        _showAddMemberDialog.value = true
    }
    
    fun hideAddDialog() {
        _showAddMemberDialog.value = false
    }
    
    fun addMember(name: String) {
        viewModelScope.launch {
            repository.addMember(name)
            _showAddMemberDialog.value = false
        }
    }
    
    fun deleteMember(member: TeamMember) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }
    
    fun exportData(): String {
        var result = ""
        viewModelScope.launch {
            result = repository.exportToJson()
            _exportResult.value = result
        }
        return result
    }
    
    suspend fun getExportJson(): String {
        return repository.exportToJson()
    }
    
    suspend fun importData(json: String): TeamRepository.ImportResult {
        return repository.importFromJson(json)
    }
    
    fun clearExportResult() {
        _exportResult.value = null
    }
    
    class Factory(private val repository: TeamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeamListViewModel(repository) as T
        }
    }
}

