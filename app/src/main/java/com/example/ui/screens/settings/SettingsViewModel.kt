package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProgramEntity
import com.example.data.repository.YouthMonitorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
  val isDemoMode: Boolean = false,
  val program: ProgramEntity? = null,
  val showEditProgramDialog: Boolean = false,
  val feedbackMessage: String? = null
)

class SettingsViewModel(private val repository: YouthMonitorRepository) : ViewModel() {

  private val _showEditProgram = MutableStateFlow(false)
  private val _feedbackMessage = MutableStateFlow<String?>(null)

  val uiState: StateFlow<SettingsUiState> = combine(
    repository.isDemoMode,
    repository.getCurrentProgram(),
    _showEditProgram,
    _feedbackMessage
  ) { isDemo, program, showEdit, msg ->
    SettingsUiState(
      isDemoMode = isDemo,
      program = program,
      showEditProgramDialog = showEdit,
      feedbackMessage = msg
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SettingsUiState()
  )

  fun toggleDemoMode() {
    val current = uiState.value.isDemoMode
    viewModelScope.launch {
      if (!current) {
        repository.seedDemoData()
      }
      repository.setDemoMode(!current)
    }
  }

  fun setEditProgramVisible(visible: Boolean) {
    _showEditProgram.value = visible
  }

  fun saveProgram(name: String, description: String, organization: String) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      val p = ProgramEntity(
        id = if (isDemo) "demo_program_01" else "prod_program_01",
        name = name.trim(),
        description = description.trim(),
        organization = organization.trim(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isDemo = isDemo
      )
      repository.saveProgram(p)
      _showEditProgram.value = false
      _feedbackMessage.value = "Program configuration updated."
    }
  }

  fun loadDemoData() {
    viewModelScope.launch {
      repository.seedDemoData()
      repository.setDemoMode(true)
      _feedbackMessage.value = "Demo dataset loaded with deterministic seed (42)."
    }
  }

  fun clearDemoData() {
    viewModelScope.launch {
      repository.clearDemoData()
      repository.setDemoMode(false)
      _feedbackMessage.value = "Demo records removed. Returned to Real Data mode."
    }
  }

  fun dismissFeedback() {
    _feedbackMessage.value = null
  }

  class Factory(private val repository: YouthMonitorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return SettingsViewModel(repository) as T
    }
  }
}
