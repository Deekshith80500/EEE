package com.example.domain.analytics

import com.example.core.model.ParticipationStatus
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.ParticipationEntity
import java.util.Locale

data class ParticipationSummary(
  val totalRecords: Int,
  val participatedCount: Int,
  val didNotParticipateCount: Int,
  val notRecordedCount: Int,
  val eligibleRecordedCount: Int,
  val participationRate: Double?, // null if eligibleRecordedCount == 0
  val denominatorDescription: String,
  val statusMessage: String
)

data class EngagementSummary(
  val isConfigured: Boolean,
  val definitionName: String?,
  val unit: String?,
  val scaleMin: Double?,
  val scaleMax: Double?,
  val totalRecords: Int,
  val averageScore: Double?,
  val minScore: Double?,
  val maxScore: Double?,
  val statusMessage: String
)

data class ParticipantAssessmentComparison(
  val participantId: String,
  val indicatorName: String,
  val beforeScore: Double?,
  val afterScore: Double?,
  val absoluteChange: Double?, // after - before
  val relativePercentageChange: Double?, // null if before == 0 or missing
  val statusMessage: String
)

data class SkillAwarenessSummary(
  val indicatorName: String?,
  val unit: String?,
  val totalPreAssessments: Int,
  val totalPostAssessments: Int,
  val pairedParticipantCount: Int,
  val averageBeforeScore: Double?,
  val averageAfterScore: Double?,
  val averageAbsoluteChange: Double?,
  val averagePercentageChange: Double?,
  val statusMessage: String
)

data class TrendPoint(
  val activityId: String,
  val activityTitle: String,
  val scheduledAt: Long,
  val participatedCount: Int,
  val eligibleCount: Int,
  val ratePercentage: Double?
)

data class TrendSummary(
  val hasSufficientData: Boolean,
  val points: List<TrendPoint>,
  val statusMessage: String
)

data class StructuredInsight(
  val category: String, // "Participation", "Engagement", "Assessment"
  val fact: String,
  val interpretation: String,
  val suggestion: String
)

object AnalyticsEngine {

  fun calculateParticipation(records: List<ParticipationEntity>): ParticipationSummary {
    if (records.isEmpty()) {
      return ParticipationSummary(
        totalRecords = 0,
        participatedCount = 0,
        didNotParticipateCount = 0,
        notRecordedCount = 0,
        eligibleRecordedCount = 0,
        participationRate = null,
        denominatorDescription = "Participated / (Participated + Did Not Participate)",
        statusMessage = "No participation data recorded yet."
      )
    }

    var participated = 0
    var didNotParticipate = 0
    var notRecorded = 0

    for (record in records) {
      when (record.status) {
        ParticipationStatus.PARTICIPATED.name -> participated++
        ParticipationStatus.DID_NOT_PARTICIPATE.name -> didNotParticipate++
        ParticipationStatus.NOT_RECORDED.name -> notRecorded++
      }
    }

    val eligibleRecorded = participated + didNotParticipate
    val rate = if (eligibleRecorded > 0) {
      (participated.toDouble() / eligibleRecorded.toDouble()) * 100.0
    } else {
      null
    }

    val message = if (rate != null) {
      String.format(
        Locale.US,
        "%.1f%% participation across %d verified records (%d pending records omitted from denominator).",
        rate,
        eligibleRecorded,
        notRecorded
      )
    } else {
      "Not enough data to calculate participation rate (all $notRecorded records are Not Recorded)."
    }

    return ParticipationSummary(
      totalRecords = records.size,
      participatedCount = participated,
      didNotParticipateCount = didNotParticipate,
      notRecordedCount = notRecorded,
      eligibleRecordedCount = eligibleRecorded,
      participationRate = rate,
      denominatorDescription = "Participated / (Participated + Did Not Participate)",
      statusMessage = message
    )
  }

  fun calculateEngagement(
    activeDefinition: EngagementDefinitionEntity?,
    records: List<EngagementRecordEntity>
  ): EngagementSummary {
    if (activeDefinition == null) {
      return EngagementSummary(
        isConfigured = false,
        definitionName = null,
        unit = null,
        scaleMin = null,
        scaleMax = null,
        totalRecords = 0,
        averageScore = null,
        minScore = null,
        maxScore = null,
        statusMessage = "Engagement measurement method not configured."
      )
    }

    val matchingRecords = records.filter { it.definitionId == activeDefinition.id }
    if (matchingRecords.isEmpty()) {
      return EngagementSummary(
        isConfigured = true,
        definitionName = activeDefinition.name,
        unit = activeDefinition.unit,
        scaleMin = activeDefinition.scaleMin,
        scaleMax = activeDefinition.scaleMax,
        totalRecords = 0,
        averageScore = null,
        minScore = null,
        maxScore = null,
        statusMessage = "Engagement method configured: '${activeDefinition.name}'. No records captured yet."
      )
    }

    val scores = matchingRecords.map { it.score }
    val avg = scores.average()
    val min = scores.minOrNull()
    val max = scores.maxOrNull()

    return EngagementSummary(
      isConfigured = true,
      definitionName = activeDefinition.name,
      unit = activeDefinition.unit,
      scaleMin = activeDefinition.scaleMin,
      scaleMax = activeDefinition.scaleMax,
      totalRecords = matchingRecords.size,
      averageScore = avg,
      minScore = min,
      maxScore = max,
      statusMessage = String.format(
        Locale.US,
        "Average %.2f %s across %d verified observation records.",
        avg,
        activeDefinition.unit,
        matchingRecords.size
      )
    )
  }

  fun calculateSkillAwareness(
    assessments: List<AssessmentEntity>,
    results: List<AssessmentResultEntity>,
    indicatorName: String? = null
  ): SkillAwarenessSummary {
    if (assessments.isEmpty() || results.isEmpty()) {
      return SkillAwarenessSummary(
        indicatorName = indicatorName,
        unit = "points",
        totalPreAssessments = 0,
        totalPostAssessments = 0,
        pairedParticipantCount = 0,
        averageBeforeScore = null,
        averageAfterScore = null,
        averageAbsoluteChange = null,
        averagePercentageChange = null,
        statusMessage = "No assessment records available yet."
      )
    }

    val preAssessmentIds = assessments.filter { it.assessmentType == "PRE_ACTIVITY" }.map { it.id }.toSet()
    val postAssessmentIds = assessments.filter { it.assessmentType == "POST_ACTIVITY" }.map { it.id }.toSet()

    val preResultsByParticipant = results
      .filter { it.assessmentId in preAssessmentIds }
      .associateBy { it.participantId }

    val postResultsByParticipant = results
      .filter { it.assessmentId in postAssessmentIds }
      .associateBy { it.participantId }

    val commonParticipants = preResultsByParticipant.keys.intersect(postResultsByParticipant.keys)

    if (commonParticipants.isEmpty()) {
      return SkillAwarenessSummary(
        indicatorName = indicatorName,
        unit = "points",
        totalPreAssessments = preResultsByParticipant.size,
        totalPostAssessments = postResultsByParticipant.size,
        pairedParticipantCount = 0,
        averageBeforeScore = null,
        averageAfterScore = null,
        averageAbsoluteChange = null,
        averagePercentageChange = null,
        statusMessage = "Comparison unavailable (requires both pre and post assessment records for matching participants)."
      )
    }

    val beforeScores = commonParticipants.mapNotNull { preResultsByParticipant[it]?.rawScore }
    val afterScores = commonParticipants.mapNotNull { postResultsByParticipant[it]?.rawScore }

    val avgBefore = beforeScores.average()
    val avgAfter = afterScores.average()
    val absChange = avgAfter - avgBefore

    val pctChange = if (avgBefore > 0.0) {
      ((avgAfter - avgBefore) / avgBefore) * 100.0
    } else {
      null // Baseline is 0, relative percentage undefined
    }

    val sign = if (absChange >= 0) "+" else ""
    val message = if (pctChange != null) {
      String.format(
        Locale.US,
        "Change: %s%.1f points (raw score difference) across %d paired assessments. Relative change: %s%.1f%%.",
        sign,
        absChange,
        commonParticipants.size,
        sign,
        pctChange
      )
    } else {
      String.format(
        Locale.US,
        "Change: %s%.1f points (raw score difference) across %d paired assessments. Relative change unavailable (baseline score is 0).",
        sign,
        absChange,
        commonParticipants.size
      )
    }

    return SkillAwarenessSummary(
      indicatorName = indicatorName,
      unit = "points",
      totalPreAssessments = preResultsByParticipant.size,
      totalPostAssessments = postResultsByParticipant.size,
      pairedParticipantCount = commonParticipants.size,
      averageBeforeScore = avgBefore,
      averageAfterScore = avgAfter,
      averageAbsoluteChange = absChange,
      averagePercentageChange = pctChange,
      statusMessage = message
    )
  }

  fun calculateParticipationTrend(
    activities: List<ActivityEntity>,
    participations: List<ParticipationEntity>
  ): TrendSummary {
    val completedActivities = activities
      .filter { it.status == "COMPLETED" || it.status == "ACTIVE" }
      .sortedBy { it.scheduledAt }

    val points = mutableListOf<TrendPoint>()
    for (act in completedActivities) {
      val actParticipations = participations.filter { it.activityId == act.id }
      if (actParticipations.isNotEmpty()) {
        val summary = calculateParticipation(actParticipations)
        points.add(
          TrendPoint(
            activityId = act.id,
            activityTitle = act.title,
            scheduledAt = act.scheduledAt,
            participatedCount = summary.participatedCount,
            eligibleCount = summary.eligibleRecordedCount,
            ratePercentage = summary.participationRate
          )
        )
      }
    }

    if (points.size < 2) {
      return TrendSummary(
        hasSufficientData = false,
        points = points,
        statusMessage = "At least 2 activity records with verified participation are required to evaluate a trend. (Currently ${points.size} available)."
      )
    }

    return TrendSummary(
      hasSufficientData = true,
      points = points,
      statusMessage = "Participation trend calculated across ${points.size} chronologically ordered activities."
    )
  }

  fun generateStructuredInsights(
    partSummary: ParticipationSummary,
    engSummary: EngagementSummary,
    skillSummary: SkillAwarenessSummary,
    trendSummary: TrendSummary
  ): List<StructuredInsight> {
    val list = mutableListOf<StructuredInsight>()

    // Participation Insight
    if (partSummary.totalRecords > 0) {
      val fact = "${partSummary.participatedCount} participants marked as Participated, ${partSummary.didNotParticipateCount} as Did Not Participate, and ${partSummary.notRecordedCount} as Not Recorded out of ${partSummary.totalRecords} total records."
      val rateStr = partSummary.participationRate?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Unavailable"
      val interp = "Calculated participation rate is $rateStr among recorded responses. ${partSummary.notRecordedCount} records were excluded from denominator to preserve data integrity."
      val sugg = if (partSummary.notRecordedCount > 0) {
        "Update the ${partSummary.notRecordedCount} 'Not recorded' entries to obtain complete roster verification."
      } else {
        "Continue verifying participant attendance within 24 hours of each session."
      }
      list.add(StructuredInsight("Participation", fact, interp, sugg))
    }

    // Engagement Insight
    if (engSummary.isConfigured && engSummary.totalRecords > 0) {
      val fact = "${engSummary.totalRecords} engagement observations were recorded using '${engSummary.definitionName}' (${engSummary.unit})."
      val avgStr = engSummary.averageScore?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"
      val interp = "The cohort average was $avgStr (range: ${engSummary.minScore} to ${engSummary.maxScore})."
      val sugg = "Review session logs for participants falling below the cohort average to evaluate mentoring pacing."
      list.add(StructuredInsight("Engagement", fact, interp, sugg))
    }

    // Skill / Awareness Insight
    if (skillSummary.pairedParticipantCount > 0) {
      val fact = "${skillSummary.pairedParticipantCount} participants completed both pre- and post-activity assessments."
      val changeStr = skillSummary.averageAbsoluteChange?.let {
        val s = if (it >= 0) "+" else ""
        String.format(Locale.US, "%s%.1f points", s, it)
      } ?: "N/A"
      val interp = "Average pre-score was ${String.format(Locale.US, "%.1f", skillSummary.averageBeforeScore)} and average post-score was ${String.format(Locale.US, "%.1f", skillSummary.averageAfterScore)}, yielding a raw score difference of $changeStr."
      val sugg = "Ensure future activities include corresponding post-assessments to maintain paired measurement integrity."
      list.add(StructuredInsight("Skill / Awareness", fact, interp, sugg))
    }

    // Trend Insight
    if (trendSummary.hasSufficientData) {
      val first = trendSummary.points.first()
      val last = trendSummary.points.last()
      val fact = "${trendSummary.points.size} activities have recorded participation data over time."
      val interp = "Earliest activity recorded ${first.participatedCount}/${first.eligibleCount} participation, while the most recent recorded ${last.participatedCount}/${last.eligibleCount}."
      val sugg = "Compare group size and session time-of-day between activities to assess attendance patterns."
      list.add(StructuredInsight("Participation Trend", fact, interp, sugg))
    }

    return list
  }
}
