package com.example.data.demo

import com.example.core.model.ParticipationStatus
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.IndicatorEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.data.local.entity.ProgramEntity
import java.util.Random

object DemoDataGenerator {
  private const val SEED = 42L

  fun generateDemoDataset(): DemoDataset {
    val random = Random(SEED)
    val now = System.currentTimeMillis()
    val dayMillis = 86_400_000L

    val program = ProgramEntity(
      id = "demo_program_01",
      name = "Youth Mentoring Pilot (Demo)",
      description = "Structured mentoring intervention measuring participation, interaction engagement, and financial awareness.",
      organization = "Youth Mentoring Initiative",
      createdAt = now - (30 * dayMillis),
      updatedAt = now,
      isDemo = true
    )

    // Generate 12 participants with data-minimized IDs and simple initials/labels
    val participants = (1..12).map { i ->
      ParticipantEntity(
        id = "demo_p_${String.format("%02d", i)}",
        programId = program.id,
        displayName = "Participant ${String.format("%02d", i)}",
        externalReference = "REF-${1000 + i}",
        groupCohort = if (i <= 6) "Cohort Alpha" else "Cohort Beta",
        active = true,
        sourceType = "DEMO",
        createdAt = now - (25 * dayMillis)
      )
    }

    // Configured Engagement definition
    val engagementDef = EngagementDefinitionEntity(
      id = "demo_eng_def_01",
      programId = program.id,
      name = "Structured Session Engagement Scale",
      methodology = "OBSERVATION_RUBRIC",
      description = "Trained mentor observation of attentiveness, dialogue, and collaborative task completion.",
      scaleMin = 1.0,
      scaleMax = 5.0,
      unit = "scale (1-5)",
      version = "1.0",
      active = true,
      createdAt = now - (20 * dayMillis)
    )

    // Configured Indicator
    val indicator = IndicatorEntity(
      id = "demo_ind_01",
      programId = program.id,
      name = "Basic Budgeting & Savings Awareness",
      category = "AWARENESS",
      definition = "Understanding of primary budgeting concepts, emergency savings, and debt avoidance.",
      unit = "score points (0-100)",
      measurementMethod = "Pre/Post 10-Item Structured Assessment",
      createdAt = now - (20 * dayMillis)
    )

    // 4 Activities across timeline
    val activities = listOf(
      ActivityEntity(
        id = "demo_act_01",
        programId = program.id,
        title = "Session 1: Goal Setting & Mentorship Foundation",
        description = "Initial rapport building, setting individual growth targets, and establishing peer guidelines.",
        scheduledAt = now - (14 * dayMillis),
        durationMinutes = 60,
        status = "COMPLETED",
        mentorName = "Mentor Sarah",
        locationOrMedium = "Community Room A",
        sourceType = "DEMO",
        createdAt = now - (20 * dayMillis)
      ),
      ActivityEntity(
        id = "demo_act_02",
        programId = program.id,
        title = "Session 2: Financial Literacy & Resource Planning",
        description = "Guided modules on needs vs wants, savings allocations, and community financial resources.",
        scheduledAt = now - (7 * dayMillis),
        durationMinutes = 75,
        status = "COMPLETED",
        mentorName = "Mentor David",
        locationOrMedium = "Learning Lab 2",
        sourceType = "DEMO",
        createdAt = now - (15 * dayMillis)
      ),
      ActivityEntity(
        id = "demo_act_03",
        programId = program.id,
        title = "Session 3: Collaborative Problem Solving Workshop",
        description = "Small-group interactive case study and peer presentation.",
        scheduledAt = now + (2 * dayMillis),
        durationMinutes = 60,
        status = "SCHEDULED",
        mentorName = "Mentor Sarah",
        locationOrMedium = "Community Room B",
        sourceType = "DEMO",
        createdAt = now - (5 * dayMillis)
      ),
      ActivityEntity(
        id = "demo_act_04",
        programId = program.id,
        title = "Session 4: Career Pathways & Next Steps",
        description = "Exploration of vocational interests, resume basics, and follow-up mentoring roadmap.",
        scheduledAt = now + (9 * dayMillis),
        durationMinutes = 90,
        status = "DRAFT",
        mentorName = "Mentor David",
        locationOrMedium = "Main Auditorium",
        sourceType = "DEMO",
        createdAt = now - (2 * dayMillis)
      )
    )

    // Participations for Act 1 and Act 2
    // Distribute PARTICIPATED, DID_NOT_PARTICIPATE, and NOT_RECORDED
    val participations = mutableListOf<ParticipationEntity>()
    val engagementRecords = mutableListOf<EngagementRecordEntity>()

    // Activity 1 Participations
    participants.forEachIndexed { index, p ->
      val status = when (index) {
        0, 1, 2, 3, 4, 5, 6, 7 -> ParticipationStatus.PARTICIPATED.name
        8, 9 -> ParticipationStatus.DID_NOT_PARTICIPATE.name
        else -> ParticipationStatus.NOT_RECORDED.name
      }
      participations.add(
        ParticipationEntity(
          id = "demo_part_1_${p.id}",
          activityId = "demo_act_01",
          participantId = p.id,
          status = status,
          recordedAt = now - (14 * dayMillis) + 3600_000,
          recordedBy = "Mentor Sarah",
          sourceReference = "Roster-01",
          sourceType = "DEMO"
        )
      )

      // Only add engagement for attendees
      if (status == ParticipationStatus.PARTICIPATED.name) {
        val score = (30 + random.nextInt(20)) / 10.0 // 3.0 to 4.9
        engagementRecords.add(
          EngagementRecordEntity(
            id = "demo_eng_1_${p.id}",
            definitionId = engagementDef.id,
            activityId = "demo_act_01",
            participantId = p.id,
            score = score,
            notes = "Observed active participation during group work.",
            recordedAt = now - (14 * dayMillis) + 3600_000,
            recordedBy = "Mentor Sarah",
            sourceType = "DEMO"
          )
        )
      }
    }

    // Activity 2 Participations
    participants.forEachIndexed { index, p ->
      val status = when (index) {
        0, 1, 2, 3, 4, 5, 6, 8, 9 -> ParticipationStatus.PARTICIPATED.name
        7 -> ParticipationStatus.DID_NOT_PARTICIPATE.name
        else -> ParticipationStatus.NOT_RECORDED.name
      }
      participations.add(
        ParticipationEntity(
          id = "demo_part_2_${p.id}",
          activityId = "demo_act_02",
          participantId = p.id,
          status = status,
          recordedAt = now - (7 * dayMillis) + 3600_000,
          recordedBy = "Mentor David",
          sourceReference = "Roster-02",
          sourceType = "DEMO"
        )
      )

      if (status == ParticipationStatus.PARTICIPATED.name) {
        val score = (35 + random.nextInt(15)) / 10.0 // 3.5 to 4.9
        engagementRecords.add(
          EngagementRecordEntity(
            id = "demo_eng_2_${p.id}",
            definitionId = engagementDef.id,
            activityId = "demo_act_02",
            participantId = p.id,
            score = score,
            notes = "Engaged in budgeting exercises.",
            recordedAt = now - (7 * dayMillis) + 3600_000,
            recordedBy = "Mentor David",
            sourceType = "DEMO"
          )
        )
      }
    }

    // Assessments for Activity 2: Pre and Post
    val preAssessment = AssessmentEntity(
      id = "demo_ass_pre_02",
      activityId = "demo_act_02",
      indicatorId = indicator.id,
      title = "Pre-Activity Financial Literacy Quiz",
      assessmentType = "PRE_ACTIVITY",
      maximumScore = 100.0,
      measurementDefinition = "10 multiple-choice questions assessing budgeting concepts.",
      version = "1.0",
      createdAt = now - (8 * dayMillis)
    )

    val postAssessment = AssessmentEntity(
      id = "demo_ass_post_02",
      activityId = "demo_act_02",
      indicatorId = indicator.id,
      title = "Post-Activity Financial Literacy Quiz",
      assessmentType = "POST_ACTIVITY",
      maximumScore = 100.0,
      measurementDefinition = "Equivalent 10 questions administered immediately following Session 2.",
      version = "1.0",
      createdAt = now - (7 * dayMillis)
    )

    val assessmentResults = mutableListOf<AssessmentResultEntity>()
    // Generate pre/post results for participants who attended Activity 2
    // Some improve, some remain steady, some have modest change (deterministic)
    val baselineScores = listOf(50.0, 60.0, 55.0, 70.0, 45.0, 65.0, 50.0, 75.0, 60.0)
    val postScores =     listOf(65.0, 75.0, 60.0, 85.0, 60.0, 70.0, 65.0, 80.0, 70.0)

    for (i in 0 until minOf(9, participants.size)) {
      val p = participants[i]
      assessmentResults.add(
        AssessmentResultEntity(
          id = "demo_res_pre_${p.id}",
          assessmentId = preAssessment.id,
          participantId = p.id,
          rawScore = baselineScores[i],
          recordedAt = now - (7 * dayMillis) - 1800_000,
          recordedBy = "Mentor David",
          sourceType = "DEMO"
        )
      )
      assessmentResults.add(
        AssessmentResultEntity(
          id = "demo_res_post_${p.id}",
          assessmentId = postAssessment.id,
          participantId = p.id,
          rawScore = postScores[i],
          recordedAt = now - (7 * dayMillis) + 3600_000,
          recordedBy = "Mentor David",
          sourceType = "DEMO"
        )
      )
    }

    return DemoDataset(
      program = program,
      participants = participants,
      engagementDefinition = engagementDef,
      indicator = indicator,
      activities = activities,
      participations = participations,
      engagementRecords = engagementRecords,
      assessments = listOf(preAssessment, postAssessment),
      assessmentResults = assessmentResults
    )
  }
}

data class DemoDataset(
  val program: ProgramEntity,
  val participants: List<ParticipantEntity>,
  val engagementDefinition: EngagementDefinitionEntity,
  val indicator: IndicatorEntity,
  val activities: List<ActivityEntity>,
  val participations: List<ParticipationEntity>,
  val engagementRecords: List<EngagementRecordEntity>,
  val assessments: List<AssessmentEntity>,
  val assessmentResults: List<AssessmentResultEntity>
)
