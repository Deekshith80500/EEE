package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.IndicatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
  // Indicators
  @Query("SELECT * FROM indicators ORDER BY name ASC")
  fun getAllIndicators(): Flow<List<IndicatorEntity>>

  @Query("SELECT * FROM indicators WHERE id = :id LIMIT 1")
  fun getIndicatorById(id: String): Flow<IndicatorEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIndicator(indicator: IndicatorEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIndicators(indicators: List<IndicatorEntity>)

  // Assessments
  @Query("SELECT * FROM assessments WHERE activityId = :activityId")
  fun getAssessmentsByActivity(activityId: String): Flow<List<AssessmentEntity>>

  @Query("SELECT * FROM assessments WHERE id = :id LIMIT 1")
  fun getAssessmentById(id: String): Flow<AssessmentEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAssessment(assessment: AssessmentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAssessments(assessments: List<AssessmentEntity>)

  // Results
  @Query("SELECT * FROM assessment_results WHERE assessmentId = :assessmentId")
  fun getResultsByAssessment(assessmentId: String): Flow<List<AssessmentResultEntity>>

  @Query("SELECT * FROM assessment_results WHERE sourceType != 'DEMO'")
  fun getAllRealResults(): Flow<List<AssessmentResultEntity>>

  @Query("SELECT * FROM assessment_results WHERE sourceType = 'DEMO'")
  fun getAllDemoResults(): Flow<List<AssessmentResultEntity>>

  @Query("SELECT * FROM assessment_results WHERE participantId = :participantId")
  fun getResultsByParticipant(participantId: String): Flow<List<AssessmentResultEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResult(result: AssessmentResultEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResults(results: List<AssessmentResultEntity>)

  @Query("DELETE FROM assessment_results WHERE sourceType = 'DEMO'")
  suspend fun deleteDemoResults()

  @Query("DELETE FROM assessments WHERE id LIKE 'demo_%'")
  suspend fun deleteDemoAssessments()

  @Query("DELETE FROM indicators WHERE id LIKE 'demo_%'")
  suspend fun deleteDemoIndicators()
}
