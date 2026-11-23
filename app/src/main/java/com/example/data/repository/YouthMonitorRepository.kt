package com.example.data.repository

import com.example.core.model.ParticipationStatus
import com.example.core.model.SourceType
import com.example.data.demo.DemoDataGenerator
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.IndicatorEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.data.local.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class YouthMonitorRepository(private val database: AppDatabase) {

  private val _isDemoMode = MutableStateFlow(false)
  val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

  fun setDemoMode(active: Boolean) {
    _isDemoMode.value = active
  }

  // Program
  fun getCurrentProgram(): Flow<ProgramEntity?> = database.programDao().getCurrentProgram()

  suspend fun saveProgram(program: ProgramEntity) {
    database.programDao().insertProgram(program)
  }

  // Activities
  fun getActivities(isDemo: Boolean): Flow<List<ActivityEntity>> {
    return if (isDemo) {
      database.activityDao().getDemoActivities()
    } else {
      database.activityDao().getRealActivities()
    }
  }

  fun getActivityById(id: String): Flow<ActivityEntity?> = database.activityDao().getActivityById(id)

  suspend fun saveActivity(activity: ActivityEntity) {
    database.activityDao().insertActivity(activity)
  }

  suspend fun deleteActivity(id: String) {
    database.activityDao().deleteActivityById(id)
  }

  // Participants
  fun getParticipants(isDemo: Boolean): Flow<List<ParticipantEntity>> {
    return if (isDemo) {
      database.participantDao().getDemoParticipants()
    } else {
      database.participantDao().getRealParticipants()
    }
  }

  suspend fun saveParticipant(participant: ParticipantEntity) {
    database.participantDao().insertParticipant(participant)
  }

  suspend fun deleteParticipant(id: String) {
    database.participantDao().deleteParticipantById(id)
  }

  // Participations
  fun getParticipationsByActivity(activityId: String): Flow<List<ParticipationEntity>> {
    return database.participationDao().getParticipationsByActivity(activityId)
  }

  fun getAllParticipations(isDemo: Boolean): Flow<List<ParticipationEntity>> {
    return if (isDemo) {
      database.participationDao().getAllDemoParticipations()
    } else {
      database.participationDao().getAllRealParticipations()
    }
  }

  suspend fun recordParticipation(
    activityId: String,
    participantId: String,
    status: ParticipationStatus,
    recordedBy: String = "Staff",
    isDemo: Boolean = false
  ) {
    val record = ParticipationEntity(
      id = "part_${activityId}_${participantId}",
      activityId = activityId,
      participantId = participantId,
      status = status.name,
      recordedAt = System.currentTimeMillis(),
      recordedBy = recordedBy,
      sourceType = if (isDemo) SourceType.DEMO.name else SourceType.USER_ENTERED.name
    )
    database.participationDao().insertParticipation(record)
  }

  // Engagement Definitions
  fun getActiveEngagementDefinitions(): Flow<List<EngagementDefinitionEntity>> {
    return database.engagementDao().getActiveDefinitions()
  }

  fun getAllEngagementDefinitions(): Flow<List<EngagementDefinitionEntity>> {
    return database.engagementDao().getAllDefinitions()
  }

  suspend fun saveEngagementDefinition(definition: EngagementDefinitionEntity) {
    database.engagementDao().insertDefinition(definition)
  }

  // Engagement Records
  fun getEngagementRecordsByActivity(activityId: String): Flow<List<EngagementRecordEntity>> {
    return database.engagementDao().getRecordsByActivity(activityId)
  }

  fun getAllEngagementRecords(isDemo: Boolean): Flow<List<EngagementRecordEntity>> {
    return if (isDemo) {
      database.engagementDao().getAllDemoRecords()
    } else {
      database.engagementDao().getAllRealRecords()
    }
  }

  suspend fun recordEngagement(
    activityId: String,
    participantId: String,
    definitionId: String,
    score: Double,
    notes: String? = null,
    recordedBy: String = "Staff",
    isDemo: Boolean = false
  ) {
    val record = EngagementRecordEntity(
      id = "eng_${activityId}_${participantId}_${definitionId}",
      definitionId = definitionId,
      activityId = activityId,
      participantId = participantId,
      score = score,
      notes = notes,
      recordedAt = System.currentTimeMillis(),
      recordedBy = recordedBy,
      sourceType = if (isDemo) SourceType.DEMO.name else SourceType.USER_ENTERED.name
    )
    database.engagementDao().insertRecord(record)
  }

  // Indicators
  fun getAllIndicators(): Flow<List<IndicatorEntity>> = database.assessmentDao().getAllIndicators()

  suspend fun saveIndicator(indicator: IndicatorEntity) {
    database.assessmentDao().insertIndicator(indicator)
  }

  // Assessments
  fun getAssessmentsByActivity(activityId: String): Flow<List<AssessmentEntity>> {
    return database.assessmentDao().getAssessmentsByActivity(activityId)
  }

  suspend fun saveAssessment(assessment: AssessmentEntity) {
    database.assessmentDao().insertAssessment(assessment)
  }

  // Assessment Results
  fun getResultsByAssessment(assessmentId: String): Flow<List<AssessmentResultEntity>> {
    return database.assessmentDao().getResultsByAssessment(assessmentId)
  }

  fun getAllAssessmentResults(isDemo: Boolean): Flow<List<AssessmentResultEntity>> {
    return if (isDemo) {
      database.assessmentDao().getAllDemoResults()
    } else {
      database.assessmentDao().getAllRealResults()
    }
  }

  suspend fun recordAssessmentResult(
    assessmentId: String,
    participantId: String,
    rawScore: Double,
    recordedBy: String = "Staff",
    isDemo: Boolean = false
  ) {
    val record = AssessmentResultEntity(
      id = "res_${assessmentId}_${participantId}",
      assessmentId = assessmentId,
      participantId = participantId,
      rawScore = rawScore,
      recordedAt = System.currentTimeMillis(),
      recordedBy = recordedBy,
      sourceType = if (isDemo) SourceType.DEMO.name else SourceType.USER_ENTERED.name
    )
    database.assessmentDao().insertResult(record)
  }

  // Seed & Clear Demo Data
  suspend fun seedDemoData() {
    val dataset = DemoDataGenerator.generateDemoDataset()
    database.programDao().insertProgram(dataset.program)
    database.participantDao().insertParticipants(dataset.participants)
    database.engagementDao().insertDefinition(dataset.engagementDefinition)
    database.assessmentDao().insertIndicator(dataset.indicator)
    database.activityDao().insertActivities(dataset.activities)
    database.participationDao().insertParticipations(dataset.participations)
    database.engagementDao().insertRecords(dataset.engagementRecords)
    database.assessmentDao().insertAssessments(dataset.assessments)
    database.assessmentDao().insertResults(dataset.assessmentResults)
  }

  suspend fun clearDemoData() {
    database.assessmentDao().deleteDemoResults()
    database.assessmentDao().deleteDemoAssessments()
    database.assessmentDao().deleteDemoIndicators()
    database.engagementDao().deleteDemoRecords()
    database.engagementDao().deleteDemoDefinitions()
    database.participationDao().deleteDemoParticipations()
    database.activityDao().deleteDemoActivities()
    database.participantDao().deleteDemoParticipants()
    database.programDao().deleteDemoPrograms()
  }
}
