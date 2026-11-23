package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.ProgramEntity
import com.example.data.repository.YouthMonitorRepository
import com.example.domain.analytics.AnalyticsEngine
import com.example.domain.analytics.EngagementSummary
import com.example.domain.analytics.ParticipationSummary
import com.example.domain.analytics.SkillAwarenessSummary
import com.example.ui.components.ProvenanceDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
  val isDemoMode: Boolean = false,
  val program: ProgramEntity? = null,
  val participationSummary: ParticipationSummary? = null,
  val engagementSummary: EngagementSummary? = null,
  val skillSummary: SkillAwarenessSummary? = null,
  val recentActivities: List<ActivityEntity> = emptyList(),
  val activeEngagementDef: EngagementDefinitionEntity? = null,
  val selectedProvenance: ProvenanceDetails? = null,
  val isInitialEmptyState: Boolean = false
)

class HomeViewModel(private val repository: YouthMonitorRepository) : ViewModel() {

  private val _selectedProvenance = MutableStateFlow<ProvenanceDetails?>(null)

  val uiState: StateFlow<HomeUiState> = combine(
    repository.isDemoMode,
    repository.getCurrentProgram(),
    repository.getActiveEngagementDefinitions(),
    _selectedProvenance
  ) { isDemo, program, engDefs, provDetails ->
    Quad(isDemo, program, engDefs.firstOrNull(), provDetails)
  }.combine(
    // Second combine block for data lists
    combine(
      repository.getAllParticipations(false),
      repository.getAllParticipations(true),
      repository.getActivities(false),
      repository.getActivities(true)
    ) { realParts, demoParts, realActs, demoActs ->
      DataLists(realParts, demoParts, realActs, demoActs)
    }
  ) { quad, dataLists ->
    val (isDemo, program, activeDef, provDetails) = quad
    val parts = if (isDemo) dataLists.demoParts else dataLists.realParts
    val acts = if (isDemo) dataLists.demoActs else dataLists.realActs

    val partSummary = AnalyticsEngine.calculateParticipation(parts)
    val engSummary = AnalyticsEngine.calculateEngagement(activeDef, emptyList()) // populated later with actual records

    HomeUiState(
      isDemoMode = isDemo,
      program = program,
      participationSummary = partSummary,
      engagementSummary = engSummary,
      recentActivities = acts.take(5),
      activeEngagementDef = activeDef,
      selectedProvenance = provDetails,
      isInitialEmptyState = !isDemo && acts.isEmpty() && parts.isEmpty()
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = HomeUiState()
  )

  fun toggleDemoMode() {
    val current = uiState.value.isDemoMode
    viewModelScope.launch {
      if (!current) {
        // Seeding demo data if empty
        repository.seedDemoData()
      }
      repository.setDemoMode(!current)
    }
  }

  fun showProvenance(details: ProvenanceDetails) {
    _selectedProvenance.value = details
  }

  fun dismissProvenance() {
    _selectedProvenance.value = null
  }

  fun showParticipationProvenance() {
    val summary = uiState.value.participationSummary ?: return
    val isDemo = uiState.value.isDemoMode
    val rateText = summary.participationRate?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Unavailable"

    _selectedProvenance.value = ProvenanceDetails(
      metricName = "Participation Rate",
      valueDisplayed = rateText,
      sourceClassification = if (isDemo) "DEMO" else "REAL",
      calculationMethod = "Participated Records / Eligible Recorded Records (Excluding 'Not Recorded')",
      denominatorDefinition = "Participated (${summary.participatedCount}) + Did Not Participate (${summary.didNotParticipateCount}) = ${summary.eligibleRecordedCount} valid recorded outcomes.",
      verifiedRecordCount = summary.totalRecords,
      measurementDefinition = "Explicit status record per activity-participant pairing. 'Not recorded' entries are strictly maintained as unverified rather than assumed absence.",
      timestampOrPeriod = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date()),
      integrityNotes = "Denominator omits ${summary.notRecordedCount} unrecorded entries to prevent skewing."
    )
  }

  fun showEngagementProvenance() {
    val summary = uiState.value.engagementSummary ?: return
    val isDemo = uiState.value.isDemoMode
    val avgText = summary.averageScore?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"

    _selectedProvenance.value = ProvenanceDetails(
      metricName = "Engagement Measurement",
      valueDisplayed = avgText,
      sourceClassification = if (isDemo) "DEMO" else "REAL",
      calculationMethod = "Arithmetic mean of verified observation records for active measurement method.",
      denominatorDefinition = "Total verified engagement observations (${summary.totalRecords})",
      verifiedRecordCount = summary.totalRecords,
      measurementDefinition = summary.definitionName ?: "Not configured",
      timestampOrPeriod = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date()),
      integrityNotes = "No engagement score is fabricated. Values come strictly from logged observations."
    )
  }

  fun showSkillProvenance() {
    val summary = uiState.value.skillSummary ?: return
    val isDemo = uiState.value.isDemoMode
    val changeText = summary.averageAbsoluteChange?.let {
      val s = if (it >= 0) "+" else ""
      String.format(Locale.US, "%s%.1f pts", s, it)
    } ?: "N/A"

    _selectedProvenance.value = ProvenanceDetails(
      metricName = "Skill & Awareness Change",
      valueDisplayed = changeText,
      sourceClassification = if (isDemo) "DEMO" else "REAL",
      calculationMethod = "Post-Activity Raw Score minus Pre-Activity Raw Score across paired participants.",
      denominatorDefinition = "${summary.pairedParticipantCount} participants with both pre and post assessments.",
      verifiedRecordCount = summary.pairedParticipantCount,
      measurementDefinition = "Raw score difference on standardized indicator assessments.",
      timestampOrPeriod = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date()),
      integrityNotes = "Raw point difference is explicitly reported. Not labeled as 'improvement' without verified program benchmarks."
    )
  }

  private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
  private data class DataLists(
    val realParts: List<com.example.data.local.entity.ParticipationEntity>,
    val demoParts: List<com.example.data.local.entity.ParticipationEntity>,
    val realActs: List<ActivityEntity>,
    val demoActs: List<ActivityEntity>
  )

  class Factory(private val repository: YouthMonitorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HomeViewModel(repository) as T
    }
  }
}
