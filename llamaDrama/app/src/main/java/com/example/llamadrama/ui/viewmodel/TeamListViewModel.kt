package com.example.llamadrama.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.llamadrama.data.database.TeamMember
import com.example.llamadrama.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamListViewModel(private val repository: TeamRepository) : ViewModel() {
    
    val members: StateFlow<List<TeamMember>> = repository.getAllMembersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _showAddMemberDialog = MutableStateFlow(false)
    val showAddMemberDialog: StateFlow<Boolean> = _showAddMemberDialog.asStateFlow()
    
    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()
    
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

