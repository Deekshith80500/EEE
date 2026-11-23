package com.example.ui.screens.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.SourceType
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.IndicatorEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.data.repository.YouthMonitorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class MonitoringUiState(
  val isDemoMode: Boolean = false,
  val participants: List<ParticipantEntity> = emptyList(),
  val engagementDefinitions: List<EngagementDefinitionEntity> = emptyList(),
  val indicators: List<IndicatorEntity> = emptyList(),
  val selectedTab: Int = 0, // 0: Participants, 1: Engagement Config, 2: Skill Indicators
  val showAddParticipantDialog: Boolean = false,
  val showAddEngagementDialog: Boolean = false,
  val showAddIndicatorDialog: Boolean = false
)

class MonitoringViewModel(private val repository: YouthMonitorRepository) : ViewModel() {

  private val _selectedTab = MutableStateFlow(0)
  private val _showAddParticipantDialog = MutableStateFlow(false)
  private val _showAddEngagementDialog = MutableStateFlow(false)
  private val _showAddIndicatorDialog = MutableStateFlow(false)

  val uiState: StateFlow<MonitoringUiState> = combine(
    repository.isDemoMode,
    _selectedTab,
    _showAddParticipantDialog,
    _showAddEngagementDialog,
    _showAddIndicatorDialog
  ) { isDemo, tab, showPart, showEng, showInd ->
    Quint(isDemo, tab, showPart, showEng, showInd)
  }.combine(
    combine(
      repository.getParticipants(false),
      repository.getParticipants(true),
      repository.getAllEngagementDefinitions(),
      repository.getAllIndicators()
    ) { realParts, demoParts, engDefs, inds ->
      DataBundle(realParts, demoParts, engDefs, inds)
    }
  ) { quint, bundle ->
    val (isDemo, tab, showPart, showEng, showInd) = quint
    val parts = if (isDemo) bundle.demoParts else bundle.realParts

    MonitoringUiState(
      isDemoMode = isDemo,
      participants = parts,
      engagementDefinitions = bundle.engDefs,
      indicators = bundle.inds,
      selectedTab = tab,
      showAddParticipantDialog = showPart,
      showAddEngagementDialog = showEng,
      showAddIndicatorDialog = showInd
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = MonitoringUiState()
  )

  fun setSelectedTab(index: Int) {
    _selectedTab.value = index
  }

  fun setAddParticipantVisible(visible: Boolean) {
    _showAddParticipantDialog.value = visible
  }

  fun setAddEngagementVisible(visible: Boolean) {
    _showAddEngagementDialog.value = visible
  }

  fun setAddIndicatorVisible(visible: Boolean) {
    _showAddIndicatorDialog.value = visible
  }

  fun addParticipant(displayName: String, ref: String?, cohort: String?) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      val p = ParticipantEntity(
        id = "part_${UUID.randomUUID().toString().take(8)}",
        programId = if (isDemo) "demo_program_01" else "prod_program_01",
        displayName = displayName.trim(),
        externalReference = ref?.trim()?.ifBlank { null },
        groupCohort = cohort?.trim()?.ifBlank { null },
        active = true,
        sourceType = if (isDemo) SourceType.DEMO.name else SourceType.USER_ENTERED.name,
        createdAt = System.currentTimeMillis()
      )
      repository.saveParticipant(p)
      _showAddParticipantDialog.value = false
    }
  }

  fun saveEngagementDefinition(
    name: String,
    methodology: String,
    scaleMin: Double,
    scaleMax: Double,
    unit: String,
    description: String
  ) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      val def = EngagementDefinitionEntity(
        id = "eng_def_${UUID.randomUUID().toString().take(8)}",
        programId = if (isDemo) "demo_program_01" else "prod_program_01",
        name = name.trim(),
        methodology = methodology,
        description = description.trim(),
        scaleMin = scaleMin,
        scaleMax = scaleMax,
        unit = unit.trim(),
        version = "1.0",
        active = true,
        createdAt = System.currentTimeMillis()
      )
      repository.saveEngagementDefinition(def)
      _showAddEngagementDialog.value = false
    }
  }

  fun saveIndicator(
    name: String,
    category: String,
    definition: String,
    unit: String,
    method: String
  ) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      val ind = IndicatorEntity(
        id = "ind_${UUID.randomUUID().toString().take(8)}",
        programId = if (isDemo) "demo_program_01" else "prod_program_01",
        name = name.trim(),
        category = category,
        definition = definition.trim(),
        unit = unit.trim(),
        measurementMethod = method.trim(),
        createdAt = System.currentTimeMillis()
      )
      repository.saveIndicator(ind)
      _showAddIndicatorDialog.value = false
    }
  }

  private data class Quint<A, B, C, D, E>(
    val first: A, val second: B, val third: C, val fourth: D, val fifth: E
  )
  private data class DataBundle(
    val realParts: List<ParticipantEntity>,
    val demoParts: List<ParticipantEntity>,
    val engDefs: List<EngagementDefinitionEntity>,
    val inds: List<IndicatorEntity>
  )

  class Factory(private val repository: YouthMonitorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MonitoringViewModel(repository) as T
    }
  }
}
