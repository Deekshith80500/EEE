package com.example.ui.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.ActivityStatus
import com.example.core.model.ParticipationStatus
import com.example.core.model.SourceType
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.IndicatorEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.data.repository.YouthMonitorRepository
import com.example.domain.analytics.AnalyticsEngine
import com.example.domain.analytics.ParticipationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ActivitiesUiState(
  val isDemoMode: Boolean = false,
  val allActivities: List<ActivityEntity> = emptyList(),
  val filterIndex: Int = 0, // 0: All, 1: Upcoming, 2: Completed
  val showCreateDialog: Boolean = false
)

data class ActivityDetailUiState(
  val activity: ActivityEntity? = null,
  val participants: List<ParticipantEntity> = emptyList(),
  val participations: Map<String, ParticipationEntity> = emptyMap(),
  val participationSummary: ParticipationSummary? = null,
  val activeEngagementDef: EngagementDefinitionEntity? = null,
  val engagementRecords: Map<String, EngagementRecordEntity> = emptyMap(),
  val assessments: List<AssessmentEntity> = emptyList(),
  val assessmentResults: List<AssessmentResultEntity> = emptyList(),
  val indicators: List<IndicatorEntity> = emptyList(),
  val selectedTab: Int = 0, // 0: Overview, 1: Participation, 2: Engagement, 3: Assessments
  val isDemoMode: Boolean = false,
  val showAddAssessmentDialog: Boolean = false
)

class ActivitiesViewModel(private val repository: YouthMonitorRepository) : ViewModel() {

  private val _filterIndex = MutableStateFlow(0)
  private val _showCreateDialog = MutableStateFlow(false)

  val uiState: StateFlow<ActivitiesUiState> = combine(
    repository.isDemoMode,
    _filterIndex,
    _showCreateDialog
  ) { isDemo, filter, showDialog ->
    Triple(isDemo, filter, showDialog)
  }.combine(
    combine(
      repository.getActivities(false),
      repository.getActivities(true)
    ) { real, demo -> Pair(real, demo) }
  ) { triple, actsPair ->
    val (isDemo, filter, showDialog) = triple
    val acts = if (isDemo) actsPair.second else actsPair.first

    val filtered = when (filter) {
      1 -> acts.filter { it.status == ActivityStatus.SCHEDULED.name || it.status == ActivityStatus.ACTIVE.name }
      2 -> acts.filter { it.status == ActivityStatus.COMPLETED.name }
      else -> acts
    }

    ActivitiesUiState(
      isDemoMode = isDemo,
      allActivities = filtered,
      filterIndex = filter,
      showCreateDialog = showDialog
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ActivitiesUiState()
  )

  fun setFilter(index: Int) {
    _filterIndex.value = index
  }

  fun setCreateDialogVisible(visible: Boolean) {
    _showCreateDialog.value = visible
  }

  fun createActivity(
    title: String,
    description: String,
    durationMinutes: Int,
    mentorName: String,
    location: String,
    status: ActivityStatus
  ) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      val newActivity = ActivityEntity(
        id = "act_${UUID.randomUUID().toString().take(8)}",
        programId = if (isDemo) "demo_program_01" else "prod_program_01",
        title = title.trim(),
        description = description.trim(),
        scheduledAt = System.currentTimeMillis() + (86400000L), // Tomorrow by default
        durationMinutes = durationMinutes,
        status = status.name,
        mentorName = mentorName.trim(),
        locationOrMedium = location.trim(),
        sourceType = if (isDemo) SourceType.DEMO.name else SourceType.USER_ENTERED.name,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      repository.saveActivity(newActivity)
      _showCreateDialog.value = false
    }
  }

  // Activity Detail State & Operations
  private val _detailSelectedTab = MutableStateFlow(0)
  val detailSelectedTab: StateFlow<Int> = _detailSelectedTab.asStateFlow()

  fun setDetailTab(tab: Int) {
    _detailSelectedTab.value = tab
  }

  fun recordParticipation(activityId: String, participantId: String, status: ParticipationStatus) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      repository.recordParticipation(
        activityId = activityId,
        participantId = participantId,
        status = status,
        recordedBy = "Staff",
        isDemo = isDemo
      )
    }
  }

  fun recordEngagement(activityId: String, participantId: String, definitionId: String, score: Double, notes: String?) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      repository.recordEngagement(
        activityId = activityId,
        participantId = participantId,
        definitionId = definitionId,
        score = score,
        notes = notes,
        isDemo = isDemo
      )
    }
  }

  fun recordAssessmentScore(assessmentId: String, participantId: String, score: Double) {
    viewModelScope.launch {
      val isDemo = repository.isDemoMode.value
      repository.recordAssessmentResult(
        assessmentId = assessmentId,
        participantId = participantId,
        rawScore = score,
        isDemo = isDemo
      )
    }
  }

  fun updateActivityStatus(activity: ActivityEntity, newStatus: ActivityStatus) {
    viewModelScope.launch {
      repository.saveActivity(activity.copy(status = newStatus.name, updatedAt = System.currentTimeMillis()))
    }
  }

  fun deleteActivity(id: String) {
    viewModelScope.launch {
      repository.deleteActivity(id)
    }
  }

  class Factory(private val repository: YouthMonitorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ActivitiesViewModel(repository) as T
    }
  }
}
