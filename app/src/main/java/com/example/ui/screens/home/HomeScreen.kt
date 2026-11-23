package com.example.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.ActivityStatus
import com.example.data.local.entity.ActivityEntity
import com.example.ui.components.ActivityStatusBadge
import com.example.ui.components.AppleCard
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.ProvenanceDialog
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  onNavigateToActivityDetail: (String) -> Unit,
  onNavigateToActivities: () -> Unit,
  onNavigateToMonitoring: () -> Unit,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  // Provenance Dialog if clicked
  state.selectedProvenance?.let { prov ->
    ProvenanceDialog(
      details = prov,
      onDismiss = { viewModel.dismissProvenance() }
    )
  }

  val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 4..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppleBgLight)
  ) {
    AppTopBar(
      title = "Youth Monitor",
      subtitle = state.program?.name ?: "Mentoring Program Overview",
      isDemoMode = state.isDemoMode,
      onToggleDemoMode = { viewModel.toggleDemoMode() }
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Greeting header
      item {
        Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
          Text(
            text = greeting.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Program at a Glance",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
          )
        }
      }

      // 1. PARTICIPATION METRIC CARD
      item {
        val partSummary = state.participationSummary
        val isPartAvailable = partSummary != null && partSummary.participationRate != null
        val partPrimary = if (isPartAvailable) {
          String.format(Locale.US, "%.1f%%", partSummary!!.participationRate)
        } else if (partSummary != null && partSummary.totalRecords > 0) {
          "Not enough data"
        } else {
          "No data available yet"
        }

        val partSubtitle = if (isPartAvailable) {
          "${partSummary!!.participatedCount} of ${partSummary.eligibleRecordedCount} verified responses participated (${partSummary.notRecordedCount} not recorded)."
        } else {
          "Record attendance in scheduled sessions to measure participation rate."
        }

        MetricCard(
          title = "Participation",
          primaryValue = partPrimary,
          subtitle = partSubtitle,
          icon = Icons.Default.CheckCircle,
          accentColor = AppleBlue,
          isAvailable = isPartAvailable,
          onProvenanceClick = { viewModel.showParticipationProvenance() },
          modifier = Modifier.testTag("metric_card_participation")
        )
      }

      // 2. ENGAGEMENT METRIC CARD
      item {
        val engSummary = state.engagementSummary
        val isEngConfigured = state.activeEngagementDef != null
        val engPrimary = if (!isEngConfigured) {
          "Measurement method not configured"
        } else if (engSummary?.averageScore != null) {
          String.format(Locale.US, "%.2f", engSummary.averageScore)
        } else {
          "No engagement data yet"
        }

        val engSubtitle = if (!isEngConfigured) {
          "Define an engagement rubric or rating scale in Monitoring settings."
        } else if (engSummary?.averageScore != null) {
          "Average score on ${state.activeEngagementDef?.unit} across ${engSummary.totalRecords} observation records."
        } else {
          "Observation scale '${state.activeEngagementDef?.name}' configured. Awaiting records."
        }

        MetricCard(
          title = "Engagement",
          primaryValue = engPrimary,
          subtitle = engSubtitle,
          icon = Icons.Default.TrendingUp,
          accentColor = AppleTeal,
          isAvailable = engSummary?.averageScore != null,
          onProvenanceClick = { viewModel.showEngagementProvenance() },
          modifier = Modifier.testTag("metric_card_engagement")
        )
      }

      // 3. SKILL / AWARENESS METRIC CARD
      item {
        val skillSummary = state.skillSummary
        val hasPaired = skillSummary != null && skillSummary.pairedParticipantCount > 0
        val skillPrimary = if (hasPaired) {
          val change = skillSummary!!.averageAbsoluteChange ?: 0.0
          val sign = if (change >= 0) "+" else ""
          String.format(Locale.US, "%s%.1f points", sign, change)
        } else {
          "No assessment data yet"
        }

        val skillSubtitle = if (hasPaired) {
          "Raw score change across ${skillSummary!!.pairedParticipantCount} paired pre/post assessments."
        } else {
          "Pre- and post-activity assessments are required to calculate score changes."
        }

        MetricCard(
          title = "Skill / Awareness",
          primaryValue = skillPrimary,
          subtitle = skillSubtitle,
          icon = Icons.Default.Psychology,
          accentColor = Color(0xFF6366F1),
          isAvailable = hasPaired,
          onProvenanceClick = { viewModel.showSkillProvenance() },
          modifier = Modifier.testTag("metric_card_skill")
        )
      }

      // Quick Action banner if initial empty state
      if (state.isInitialEmptyState) {
        item {
          AppleCard(
            backgroundColor = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = "Welcome to Youth Monitor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "This application measures real program evidence without assuming facts. You can create your first activity or explore with isolated Demo Data.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
              )
              Spacer(modifier = Modifier.height(14.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                  onClick = { viewModel.toggleDemoMode() },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Try Demo Data", fontSize = 13.sp)
                }
                Button(
                  onClick = onNavigateToActivities,
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  border = ButtonDefaults.outlinedButtonBorder
                ) {
                  Text("New Activity", fontSize = 13.sp, color = TextPrimary)
                }
              }
            }
          }
        }
      }

      // RECENT ACTIVITIES SECTION
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Activities",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          if (state.recentActivities.isNotEmpty()) {
            Button(
              onClick = onNavigateToActivities,
              colors = ButtonDefaults.textButtonColors(),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
              Text("See All", color = AppleBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
              Icon(
                Icons.Default.ArrowForward,
                contentDescription = "See all",
                tint = AppleBlue,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }

      if (state.recentActivities.isEmpty()) {
        item {
          EmptyStateView(
            title = "No recent activities",
            message = "Structured activities will appear here once scheduled or completed.",
            actionLabel = "Create Activity",
            onActionClick = onNavigateToActivities
          )
        }
      } else {
        items(state.recentActivities, key = { it.id }) { activity ->
          ActivityItemCard(
            activity = activity,
            onClick = { onNavigateToActivityDetail(activity.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
fun ActivityItemCard(
  activity: ActivityEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateStr = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(activity.scheduledAt))

  AppleCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        ActivityStatusBadge(status = activity.status)
        if (activity.sourceType == "DEMO") {
          Text(
            text = "Demo",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFD97706)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = activity.title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      if (activity.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = activity.description,
          fontSize = 13.sp,
          color = TextSecondary,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = dateStr,
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        if (activity.mentorName.isNotBlank()) {
          Text(
            text = activity.mentorName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
          )
        }
      }
    }
  }
}
