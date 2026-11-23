package com.example.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.analytics.ParticipationSummary
import com.example.domain.analytics.StructuredInsight
import com.example.domain.analytics.TrendSummary
import com.example.ui.components.AppleCard
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProvenanceDialog
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusNeutralText
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
  viewModel: InsightsViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  state.selectedProvenance?.let { prov ->
    ProvenanceDialog(
      details = prov,
      onDismiss = { viewModel.dismissProvenance() }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppleBgLight)
  ) {
    AppTopBar(
      title = "Insights",
      subtitle = "Verified Data & Evidence",
      isDemoMode = state.isDemoMode
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. PARTICIPATION RATIO AUDIT
      item {
        val partSummary = state.participationSummary
        if (partSummary != null && partSummary.totalRecords > 0) {
          ParticipationAuditCard(
            summary = partSummary,
            onInspectFormula = { viewModel.showDenominatorProvenance() }
          )
        } else {
          AppleCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = "PARTICIPATION RATE AUDIT",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No data available yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Participation rate requires at least one verified record.",
                fontSize = 13.sp,
                color = TextMuted
              )
            }
          }
        }
      }

      // 2. PARTICIPATION TREND OVER TIME (Rule: Never show trend from 1 point!)
      item {
        val trend = state.trendSummary
        AppleCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "CHRONOLOGICAL PARTICIPATION TREND",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextMuted,
                  letterSpacing = 0.8.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trend == null || !trend.hasSufficientData) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                  .padding(14.dp)
              ) {
                Text(
                  text = "Trend Unavailable",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = trend?.statusMessage ?: "At least 2 chronological sessions are required to evaluate a trend without misleading extrapolation.",
                  fontSize = 12.sp,
                  color = TextSecondary,
                  lineHeight = 16.sp
                )
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                trend.points.forEach { pt ->
                  val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(pt.scheduledAt))
                  val pctStr = pt.ratePercentage?.let { String.format(Locale.US, "%.0f%%", it) } ?: "N/A"
                  val progress = pt.ratePercentage?.div(100.0)?.toFloat() ?: 0f

                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = "${pt.activityTitle} ($dateStr)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                      )
                      Text(
                        text = "$pctStr (${pt.participatedCount}/${pt.eligibleCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleBlue
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                    ) {
                      Box(
                        modifier = Modifier
                          .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                          .fillMaxHeight()
                          .background(AppleBlue)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 3. STRUCTURED REPORT (FACT vs INTERPRETATION vs SUGGESTION)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Verified, contentDescription = null, tint = AppleTeal, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Factual Evidence & Insights",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
        Text(
          text = "Strict separation: Stored facts, mathematical interpretations, and non-factual operational suggestions.",
          fontSize = 12.sp,
          color = TextSecondary,
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }

      if (state.structuredInsights.isEmpty()) {
        item {
          EmptyStateView(
            title = "No insights available yet",
            message = "Structured insights will be generated automatically as you record participation and observations."
          )
        }
      } else {
        items(state.structuredInsights) { insight ->
          StructuredInsightCard(insight = insight)
        }
      }

      item { Spacer(modifier = Modifier.height(32.dp)) }
    }
  }
}

@Composable
private fun ParticipationAuditCard(
  summary: ParticipationSummary,
  onInspectFormula: () -> Unit
) {
  val rateStr = summary.participationRate?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Unavailable"
  val total = summary.totalRecords.toFloat().coerceAtLeast(1f)
  val partFraction = summary.participatedCount / total
  val didNotFraction = summary.didNotParticipateCount / total
  val notRecFraction = summary.notRecordedCount / total

  AppleCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "PARTICIPATION RATE AUDIT",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextMuted,
          letterSpacing = 0.8.sp
        )
        Button(
          onClick = onInspectFormula,
          colors = ButtonDefaults.textButtonColors(),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
          Icon(Icons.Default.Info, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Inspect Formula", fontSize = 12.sp, color = AppleBlue)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = rateStr,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = summary.statusMessage,
        fontSize = 13.sp,
        color = TextSecondary,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3-color distribution bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp))
          .background(Color(0xFFE2E8F0))
      ) {
        if (summary.participatedCount > 0) {
          Box(
            modifier = Modifier
              .weight(partFraction)
              .fillMaxHeight()
              .background(Color(0xFF047857))
          )
        }
        if (summary.didNotParticipateCount > 0) {
          Box(
            modifier = Modifier
              .weight(didNotFraction)
              .fillMaxHeight()
              .background(Color(0xFFDC2626))
          )
        }
        if (summary.notRecordedCount > 0) {
          Box(
            modifier = Modifier
              .weight(notRecFraction)
              .fillMaxHeight()
              .background(Color(0xFF94A3B8))
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        LegendItem("Participated", "${summary.participatedCount}", Color(0xFF047857))
        LegendItem("Did Not Part.", "${summary.didNotParticipateCount}", Color(0xFFDC2626))
        LegendItem("Not Recorded", "${summary.notRecordedCount}", Color(0xFF94A3B8))
      }
    }
  }
}

@Composable
private fun LegendItem(label: String, count: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(color, RoundedCornerShape(2.dp))
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$label: $count",
      fontSize = 11.sp,
      color = TextSecondary
    )
  }
}

@Composable
fun StructuredInsightCard(
  insight: StructuredInsight,
  modifier: Modifier = Modifier
) {
  AppleCard(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(18.dp)) {
      Box(
        modifier = Modifier
          .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = insight.category.uppercase(),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // FACT
      InsightSection(
        badge = "FACT",
        badgeBg = Color(0xFFD1FAE5),
        badgeText = Color(0xFF047857),
        body = insight.fact
      )

      Spacer(modifier = Modifier.height(8.dp))

      // INTERPRETATION
      InsightSection(
        badge = "INTERPRETATION",
        badgeBg = Color(0xFFE0F2FE),
        badgeText = Color(0xFF0369A1),
        body = insight.interpretation
      )

      Spacer(modifier = Modifier.height(8.dp))

      // SUGGESTION
      InsightSection(
        badge = "SUGGESTION",
        badgeBg = Color(0xFFFEF3C7),
        badgeText = Color(0xFFB45309),
        body = insight.suggestion
      )
    }
  }
}

@Composable
private fun InsightSection(
  badge: String,
  badgeBg: Color,
  badgeText: Color,
  body: String
) {
  Column {
    Box(
      modifier = Modifier
        .background(badgeBg, RoundedCornerShape(4.dp))
        .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Text(
        text = badge,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = badgeText,
        letterSpacing = 0.5.sp
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = body,
      fontSize = 13.sp,
      color = TextPrimary,
      lineHeight = 18.sp
    )
  }
}
