package com.example

import com.example.core.model.ParticipationStatus
import com.example.data.demo.DemoDataGenerator
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.domain.analytics.AnalyticsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEngineTest {

  @Test
  fun testParticipationCalculation_NotRecordedIsNotDidNotParticipate() {
    val records = listOf(
      ParticipationEntity(
        id = "1",
        activityId = "act1",
        participantId = "p1",
        status = ParticipationStatus.PARTICIPATED.name
      ),
      ParticipationEntity(
        id = "2",
        activityId = "act1",
        participantId = "p2",
        status = ParticipationStatus.DID_NOT_PARTICIPATE.name
      ),
      ParticipationEntity(
        id = "3",
        activityId = "act1",
        participantId = "p3",
        status = ParticipationStatus.NOT_RECORDED.name
      ),
      ParticipationEntity(
        id = "4",
        activityId = "act1",
        participantId = "p4",
        status = ParticipationStatus.NOT_RECORDED.name
      )
    )

    val summary = AnalyticsEngine.calculateParticipation(records)

    assertEquals(4, summary.totalRecords)
    assertEquals(1, summary.participatedCount)
    assertEquals(1, summary.didNotParticipateCount)
    assertEquals(2, summary.notRecordedCount)
    // Denominator must strictly be eligible recorded: 1 + 1 = 2
    assertEquals(2, summary.eligibleRecordedCount)
    // Rate is 1 / 2 = 50.0%
    assertNotNull(summary.participationRate)
    assertEquals(50.0, summary.participationRate!!, 0.001)
  }

  @Test
  fun testParticipationCalculation_EmptyRecords() {
    val summary = AnalyticsEngine.calculateParticipation(emptyList())
    assertNull(summary.participationRate)
    assertEquals(0, summary.totalRecords)
    assertEquals("No participation data recorded yet.", summary.statusMessage)
  }

  @Test
  fun testAssessmentScoreChange_ValidValues() {
    val preAssessment = AssessmentEntity(
      id = "pre1",
      activityId = "act1",
      indicatorId = "ind1",
      title = "Pre Test",
      assessmentType = "PRE_ACTIVITY"
    )
    val postAssessment = AssessmentEntity(
      id = "post1",
      activityId = "act1",
      indicatorId = "ind1",
      title = "Post Test",
      assessmentType = "POST_ACTIVITY"
    )

    val results = listOf(
      AssessmentResultEntity(
        id = "r1",
        assessmentId = "pre1",
        participantId = "p1",
        rawScore = 60.0
      ),
      AssessmentResultEntity(
        id = "r2",
        assessmentId = "post1",
        participantId = "p1",
        rawScore = 75.0
      )
    )

    val summary = AnalyticsEngine.calculateSkillAwareness(
      assessments = listOf(preAssessment, postAssessment),
      results = results,
      indicatorName = "Confidence"
    )

    assertEquals(1, summary.pairedParticipantCount)
    assertEquals(60.0, summary.averageBeforeScore!!, 0.001)
    assertEquals(75.0, summary.averageAfterScore!!, 0.001)
    assertEquals(15.0, summary.averageAbsoluteChange!!, 0.001)
    assertEquals(25.0, summary.averagePercentageChange!!, 0.001)
  }

  @Test
  fun testAssessmentScoreChange_DivisionByZero() {
    val pre = AssessmentEntity(id = "pre", activityId = "a", indicatorId = "i", title = "Pre", assessmentType = "PRE_ACTIVITY")
    val post = AssessmentEntity(id = "post", activityId = "a", indicatorId = "i", title = "Post", assessmentType = "POST_ACTIVITY")

    val results = listOf(
      AssessmentResultEntity(id = "1", assessmentId = "pre", participantId = "p1", rawScore = 0.0),
      AssessmentResultEntity(id = "2", assessmentId = "post", participantId = "p1", rawScore = 20.0)
    )

    val summary = AnalyticsEngine.calculateSkillAwareness(listOf(pre, post), results)
    assertEquals(20.0, summary.averageAbsoluteChange!!, 0.001)
    // When baseline is 0, relative percentage change must be null (unavailable)
    assertNull(summary.averagePercentageChange)
  }

  @Test
  fun testAssessmentScoreChange_MissingData_ComparisonUnavailable() {
    val pre = AssessmentEntity(id = "pre", activityId = "a", indicatorId = "i", title = "Pre", assessmentType = "PRE_ACTIVITY")
    val post = AssessmentEntity(id = "post", activityId = "a", indicatorId = "i", title = "Post", assessmentType = "POST_ACTIVITY")

    // Participant p1 only has pre, participant p2 only has post -> No common participant!
    val results = listOf(
      AssessmentResultEntity(id = "1", assessmentId = "pre", participantId = "p1", rawScore = 50.0),
      AssessmentResultEntity(id = "2", assessmentId = "post", participantId = "p2", rawScore = 70.0)
    )

    val summary = AnalyticsEngine.calculateSkillAwareness(listOf(pre, post), results)
    assertEquals(0, summary.pairedParticipantCount)
    assertNull(summary.averageAbsoluteChange)
    assertTrue(summary.statusMessage.contains("Comparison unavailable"))
  }

  @Test
  fun testParticipationTrend_NeverFromSingleObservation() {
    val singleActivity = ActivityEntity(
      id = "act1",
      programId = "prog",
      title = "Session 1",
      status = "COMPLETED",
      scheduledAt = 1000L
    )
    val participations = listOf(
      ParticipationEntity(id = "p1", activityId = "act1", participantId = "user1", status = ParticipationStatus.PARTICIPATED.name)
    )

    val trend = AnalyticsEngine.calculateParticipationTrend(listOf(singleActivity), participations)
    assertFalse(trend.hasSufficientData)
    assertTrue(trend.statusMessage.contains("At least 2 activity records"))
  }

  @Test
  fun testDemoDataGenerator_DeterministicAndTagged() {
    val dataset1 = DemoDataGenerator.generateDemoDataset()
    val dataset2 = DemoDataGenerator.generateDemoDataset()

    assertEquals(dataset1.participants.size, dataset2.participants.size)
    assertEquals(dataset1.activities.size, dataset2.activities.size)

    dataset1.participations.forEach {
      assertEquals("DEMO", it.sourceType)
    }
    dataset1.activities.forEach {
      assertEquals("DEMO", it.sourceType)
    }
  }
}
