package com.example.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.data.repository.YouthMonitorRepository
import com.example.domain.analytics.AnalyticsEngine
import com.example.domain.analytics.EngagementSummary
import com.example.domain.analytics.ParticipationSummary
import com.example.domain.analytics.SkillAwarenessSummary
import com.example.domain.analytics.StructuredInsight
import com.example.domain.analytics.TrendSummary
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

data class InsightsUiState(
  val isDemoMode: Boolean = false,
  val participationSummary: ParticipationSummary? = null,
  val engagementSummary: EngagementSummary? = null,
  val skillSummary: SkillAwarenessSummary? = null,
  val trendSummary: TrendSummary? = null,
  val structuredInsights: List<StructuredInsight> = emptyList(),
  val selectedProvenance: ProvenanceDetails? = null,
  val totalActivitiesEvaluated: Int = 0,
  val totalParticipantsCount: Int = 0
)

class InsightsViewModel(private val repository: YouthMonitorRepository) : ViewModel() {

  private val _selectedProvenance = MutableStateFlow<ProvenanceDetails?>(null)

  val uiState: StateFlow<InsightsUiState> = combine(
    repository.isDemoMode,
    repository.getActiveEngagementDefinitions(),
    repository.getAllIndicators(),
    _selectedProvenance
  ) { isDemo, engDefs, indicators, prov ->
    QuadBundle(isDemo, engDefs.firstOrNull(), indicators.firstOrNull()?.name, prov)
  }.combine(
    combine(
      repository.getAllParticipations(false),
      repository.getAllParticipations(true),
      repository.getActivities(false),
      repository.getActivities(true)
    ) { rParts, dParts, rActs, dActs ->
      PartActBundle(rParts, dParts, rActs, dActs)
    }
  ) { quad, partAct ->
    Pair(quad, partAct)
  }.combine(
    combine(
      repository.getAllEngagementRecords(false),
      repository.getAllEngagementRecords(true),
      repository.getAllAssessmentResults(false),
      repository.getAllAssessmentResults(true)
    ) { rEng, dEng, rAss, dAss ->
      EngAssBundle(rEng, dEng, rAss, dAss)
    }
  ) { pair, engAss ->
    val (quad, partAct) = pair
    val isDemo = quad.isDemo
    val activeDef = quad.activeDef
    val indName = quad.indName
    val prov = quad.prov

    val parts = if (isDemo) partAct.dParts else partAct.rParts
    val acts = if (isDemo) partAct.dActs else partAct.rActs
    val engRecords = if (isDemo) engAss.dEng else engAss.rEng
    val assResults = if (isDemo) engAss.dAss else engAss.rAss

    val partSummary = AnalyticsEngine.calculateParticipation(parts)
    val engSummary = AnalyticsEngine.calculateEngagement(activeDef, engRecords)
    val skillSummary = AnalyticsEngine.calculateSkillAwareness(emptyList(), assResults, indName)
    val trendSummary = AnalyticsEngine.calculateParticipationTrend(acts, parts)

    val insights = AnalyticsEngine.generateStructuredInsights(
      partSummary,
      engSummary,
      skillSummary,
      trendSummary
    )

    InsightsUiState(
      isDemoMode = isDemo,
      participationSummary = partSummary,
      engagementSummary = engSummary,
      skillSummary = skillSummary,
      trendSummary = trendSummary,
      structuredInsights = insights,
      selectedProvenance = prov,
      totalActivitiesEvaluated = acts.size,
      totalParticipantsCount = parts.map { it.participantId }.distinct().size
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = InsightsUiState()
  )

  fun showProvenance(details: ProvenanceDetails) {
    _selectedProvenance.value = details
  }

  fun dismissProvenance() {
    _selectedProvenance.value = null
  }

  fun showDenominatorProvenance() {
    val summary = uiState.value.participationSummary ?: return
    val isDemo = uiState.value.isDemoMode
    val rateText = summary.participationRate?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Unavailable"

    _selectedProvenance.value = ProvenanceDetails(
      metricName = "Participation Rate Formula Audit",
      valueDisplayed = rateText,
      sourceClassification = if (isDemo) "DEMO" else "REAL",
      calculationMethod = "Participated / (Participated + Did Not Participate)",
      denominatorDefinition = "${summary.participatedCount} Participated + ${summary.didNotParticipateCount} Did Not Participate = ${summary.eligibleRecordedCount} eligible recorded instances.",
      verifiedRecordCount = summary.totalRecords,
      measurementDefinition = "Explicitly excludes ${summary.notRecordedCount} unrecorded instances from the denominator to ensure statistical accuracy.",
      timestampOrPeriod = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date()),
      integrityNotes = "Rule verified: 'Not recorded' is strictly unverified, never assumed absence."
    )
  }

  private data class QuadBundle(
    val isDemo: Boolean,
    val activeDef: EngagementDefinitionEntity?,
    val indName: String?,
    val prov: ProvenanceDetails?
  )

  private data class PartActBundle(
    val rParts: List<ParticipationEntity>,
    val dParts: List<ParticipationEntity>,
    val rActs: List<ActivityEntity>,
    val dActs: List<ActivityEntity>
  )

  private data class EngAssBundle(
    val rEng: List<EngagementRecordEntity>,
    val dEng: List<EngagementRecordEntity>,
    val rAss: List<AssessmentResultEntity>,
    val dAss: List<AssessmentResultEntity>
  )

  class Factory(private val repository: YouthMonitorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return InsightsViewModel(repository) as T
    }
  }
}
