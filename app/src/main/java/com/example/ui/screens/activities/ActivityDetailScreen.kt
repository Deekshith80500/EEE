package com.example.ui.screens.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.ActivityStatus
import com.example.core.model.ParticipationStatus
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.domain.analytics.AnalyticsEngine
import com.example.domain.analytics.ParticipationSummary
import com.example.ui.components.ActivityStatusBadge
import com.example.ui.components.AppleCard
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ParticipationBadge
import com.example.ui.components.SegmentedControl
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorder
import com.example.ui.theme.AppleSurfaceLight
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusNeutralBg
import com.example.ui.theme.StatusNeutralText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityDetailScreen(
  activity: ActivityEntity?,
  participants: List<ParticipantEntity>,
  participations: List<ParticipationEntity>,
  activeEngagementDef: EngagementDefinitionEntity?,
  engagementRecords: List<EngagementRecordEntity>,
  assessments: List<AssessmentEntity>,
  assessmentResults: List<AssessmentResultEntity>,
  onBackClick: () -> Unit,
  onRecordParticipation: (participantId: String, status: ParticipationStatus) -> Unit,
  onRecordEngagement: (participantId: String, definitionId: String, score: Double, notes: String?) -> Unit,
  onRecordAssessmentScore: (assessmentId: String, participantId: String, score: Double) -> Unit,
  onUpdateStatus: (newStatus: ActivityStatus) -> Unit,
  onDeleteActivity: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (activity == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Activity not found.", color = TextSecondary)
    }
    return
  }

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Overview", "Participation", "Engagement", "Assessments")

  val participationMap = remember(participations) {
    participations.associateBy { it.participantId }
  }
  val engagementMap = remember(engagementRecords) {
    engagementRecords.associateBy { it.participantId }
  }

  val partSummary = remember(participations) {
    AnalyticsEngine.calculateParticipation(participations)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppleBgLight)
  ) {
    AppTopBar(
      title = activity.title,
      subtitle = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(activity.scheduledAt)),
      isDemoMode = activity.sourceType == "DEMO",
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
      SegmentedControl(
        items = tabs,
        selectedIndex = selectedTab,
        onItemSelected = { selectedTab = it }
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    when (selectedTab) {
      0 -> ActivityOverviewTab(
        activity = activity,
        partSummary = partSummary,
        onUpdateStatus = onUpdateStatus,
        onDeleteActivity = onDeleteActivity
      )
      1 -> ActivityParticipationTab(
        participants = participants,
        participations = participationMap,
        onRecordParticipation = onRecordParticipation
      )
      2 -> ActivityEngagementTab(
        participants = participants,
        participations = participationMap,
        activeDef = activeEngagementDef,
        engagementRecords = engagementMap,
        onRecordEngagement = onRecordEngagement
      )
      3 -> ActivityAssessmentsTab(
        participants = participants,
        assessments = assessments,
        results = assessmentResults,
        onRecordScore = onRecordAssessmentScore
      )
    }
  }
}

@Composable
private fun ActivityOverviewTab(
  activity: ActivityEntity,
  partSummary: ParticipationSummary,
  onUpdateStatus: (ActivityStatus) -> Unit,
  onDeleteActivity: () -> Unit
) {
  var showDeleteConfirm by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SESSION STATUS",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextMuted,
              letterSpacing = 0.8.sp
            )
            ActivityStatusBadge(status = activity.status)
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Status switcher pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ActivityStatus.values().forEach { status ->
              val isSelected = activity.status == status.name
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) AppleBlue else Color(0xFFF1F5F9))
                  .clickable { onUpdateStatus(status) }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = status.label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else TextSecondary
                )
              }
            }
          }
        }
      }
    }

    item {
      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "SESSION DETAILS",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          DetailRow("Mentor", activity.mentorName.ifBlank { "Unassigned" })
          DetailRow("Duration", "${activity.durationMinutes} minutes")
          DetailRow("Location", activity.locationOrMedium.ifBlank { "Unspecified" })
          DetailRow("Data Source", activity.sourceType)

          if (activity.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Objective / Plan", fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = activity.description, fontSize = 14.sp, color = TextPrimary)
          }
        }
      }
    }

    item {
      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "VERIFIED PARTICIPATION AUDIT",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          val rateStr = partSummary.participationRate?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Unavailable"
          Text(
            text = rateStr,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${partSummary.participatedCount} Participated • ${partSummary.didNotParticipateCount} Did Not Participate • ${partSummary.notRecordedCount} Not Recorded",
            fontSize = 13.sp,
            color = TextSecondary
          )
        }
      }
    }

    item {
      Button(
        onClick = { showDeleteConfirm = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("btn_delete_activity")
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete Activity", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
      }
    }

    item { Spacer(modifier = Modifier.height(32.dp)) }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete Activity?") },
      text = { Text("This will remove this activity and its linked records. This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDeleteActivity()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        Button(
          onClick = { showDeleteConfirm = false },
          colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 13.sp, color = TextSecondary)
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
  }
}

@Composable
private fun ActivityParticipationTab(
  participants: List<ParticipantEntity>,
  participations: Map<String, ParticipationEntity>,
  onRecordParticipation: (participantId: String, status: ParticipationStatus) -> Unit
) {
  if (participants.isEmpty()) {
    EmptyStateView(
      title = "No participants registered",
      message = "Register participants in Monitoring to take attendance for this activity."
    )
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text(
        text = "Tap a status pill to record attendance. 'Not recorded' is strictly tracked and never converted to absence.",
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
      )
    }

    items(participants, key = { it.id }) { participant ->
      val currentStatusName = participations[participant.id]?.status ?: ParticipationStatus.NOT_RECORDED.name
      val currentStatus = try {
        ParticipationStatus.valueOf(currentStatusName)
      } catch (e: Exception) {
        ParticipationStatus.NOT_RECORDED
      }

      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = participant.displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              participant.externalReference?.let {
                Text(text = it, fontSize = 11.sp, color = TextMuted)
              }
            }
            ParticipationBadge(status = currentStatus)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 3-way toggle buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            ParticipationStatus.values().forEach { status ->
              val isSelected = currentStatus == status
              val bg = when (status) {
                ParticipationStatus.PARTICIPATED -> if (isSelected) StatusSuccessText else StatusSuccessBg
                ParticipationStatus.DID_NOT_PARTICIPATE -> if (isSelected) StatusErrorText else StatusErrorBg
                ParticipationStatus.NOT_RECORDED -> if (isSelected) Color(0xFF475569) else StatusNeutralBg
              }
              val textColor = if (isSelected) Color.White else when (status) {
                ParticipationStatus.PARTICIPATED -> StatusSuccessText
                ParticipationStatus.DID_NOT_PARTICIPATE -> StatusErrorText
                ParticipationStatus.NOT_RECORDED -> StatusNeutralText
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(bg)
                  .clickable { onRecordParticipation(participant.id, status) }
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = status.label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = textColor
                )
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(32.dp)) }
  }
}

@Composable
private fun ActivityEngagementTab(
  participants: List<ParticipantEntity>,
  participations: Map<String, ParticipationEntity>,
  activeDef: EngagementDefinitionEntity?,
  engagementRecords: Map<String, EngagementRecordEntity>,
  onRecordEngagement: (participantId: String, definitionId: String, score: Double, notes: String?) -> Unit
) {
  if (activeDef == null) {
    EmptyStateView(
      title = "Engagement measurement method not configured",
      message = "Define an engagement observation method and scale in Monitoring settings to begin recording engagement."
    )
    return
  }

  // Filter only attendees (status == PARTICIPATED)
  val attendees = participants.filter {
    participations[it.id]?.status == ParticipationStatus.PARTICIPATED.name
  }

  if (attendees.isEmpty()) {
    EmptyStateView(
      title = "No attendees marked yet",
      message = "Engagement is measured for participants who attended. Mark attendance first in the Participation tab."
    )
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      AppleCard(backgroundColor = Color(0xFFF0FDFA), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "CONFIGURED METHOD: ${activeDef.name.uppercase()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F766E)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${activeDef.description} (Scale: ${activeDef.scaleMin.toInt()} to ${activeDef.scaleMax.toInt()} ${activeDef.unit})",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }

    items(attendees, key = { it.id }) { participant ->
      val currentRecord = engagementRecords[participant.id]
      var score by remember(currentRecord) {
        mutableStateOf(currentRecord?.score ?: activeDef.scaleMin)
      }
      var notes by remember(currentRecord) {
        mutableStateOf(currentRecord?.notes ?: "")
      }

      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = participant.displayName,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
            Text(
              text = String.format(Locale.US, "%.1f %s", score, activeDef.unit),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = AppleTeal
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Slider for score
          Slider(
            value = score.toFloat(),
            onValueChange = { score = it.toDouble() },
            valueRange = activeDef.scaleMin.toFloat()..activeDef.scaleMax.toFloat(),
            steps = ((activeDef.scaleMax - activeDef.scaleMin) * 2).toInt() - 1,
            colors = SliderDefaults.colors(
              thumbColor = AppleTeal,
              activeTrackColor = AppleTeal
            )
          )

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("Observation notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = AppleBorder,
              focusedBorderColor = AppleTeal
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = {
              onRecordEngagement(participant.id, activeDef.id, score, notes.ifBlank { null })
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppleTeal),
            modifier = Modifier.align(Alignment.End)
          ) {
            Text("Save Observation", fontSize = 12.sp, color = Color.White)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(32.dp)) }
  }
}

@Composable
private fun ActivityAssessmentsTab(
  participants: List<ParticipantEntity>,
  assessments: List<AssessmentEntity>,
  results: List<AssessmentResultEntity>,
  onRecordScore: (assessmentId: String, participantId: String, score: Double) -> Unit
) {
  if (assessments.isEmpty()) {
    EmptyStateView(
      title = "No assessments linked to this session",
      message = "Define pre- and post-activity assessments in Monitoring to measure skill and awareness changes."
    )
    return
  }

  val preAssessment = assessments.firstOrNull { it.assessmentType == "PRE_ACTIVITY" }
  val postAssessment = assessments.firstOrNull { it.assessmentType == "POST_ACTIVITY" }

  val resultsByAssAndPart = remember(results) {
    results.associateBy { "${it.assessmentId}_${it.participantId}" }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      AppleCard(backgroundColor = Color(0xFFEEF2FF), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "STRUCTURED PRE / POST ASSESSMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F46E5)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Enter raw assessment scores (0-100). The system computes exact score differences without fabricating outcome claims.",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }

    items(participants, key = { it.id }) { participant ->
      val preResult = preAssessment?.let { resultsByAssAndPart["${it.id}_${participant.id}"] }
      val postResult = postAssessment?.let { resultsByAssAndPart["${it.id}_${participant.id}"] }

      var preInput by remember(preResult) { mutableStateOf(preResult?.rawScore?.toInt()?.toString() ?: "") }
      var postInput by remember(postResult) { mutableStateOf(postResult?.rawScore?.toInt()?.toString() ?: "") }

      val before = preResult?.rawScore
      val after = postResult?.rawScore
      val diff = if (before != null && after != null) after - before else null

      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = participant.displayName,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
            if (diff != null) {
              val sign = if (diff >= 0) "+" else ""
              val color = if (diff >= 0) Color(0xFF047857) else Color(0xFFDC2626)
              Text(
                text = "$sign${String.format(Locale.US, "%.0f", diff)} pts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
              )
            } else {
              Text(
                text = "Comparison unavailable",
                fontSize = 11.sp,
                color = TextMuted
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Pre-score input
            if (preAssessment != null) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Pre-Score (0-100)", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = preInput,
                  onValueChange = {
                    preInput = it
                    it.toDoubleOrNull()?.let { score ->
                      onRecordScore(preAssessment.id, participant.id, score)
                    }
                  },
                  placeholder = { Text("e.g. 60") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppleBorder,
                    focusedBorderColor = AppleBlue
                  )
                )
              }
            }

            // Post-score input
            if (postAssessment != null) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Post-Score (0-100)", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = postInput,
                  onValueChange = {
                    postInput = it
                    it.toDoubleOrNull()?.let { score ->
                      onRecordScore(postAssessment.id, participant.id, score)
                    }
                  },
                  placeholder = { Text("e.g. 75") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppleBorder,
                    focusedBorderColor = AppleBlue
                  )
                )
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(32.dp)) }
  }
}
