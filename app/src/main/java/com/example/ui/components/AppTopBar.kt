package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorder
import com.example.ui.theme.DemoBadgeBg
import com.example.ui.theme.DemoBadgeBorder
import com.example.ui.theme.DemoBadgeText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppTopBar(
  title: String,
  subtitle: String? = null,
  isDemoMode: Boolean = false,
  onToggleDemoMode: (() -> Unit)? = null,
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(AppleBgLight)
      .statusBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Mode Banner if in Demo mode
    if (isDemoMode) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DemoBadgeBg)
          .border(1.dp, DemoBadgeBorder, RoundedCornerShape(12.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(Color(0xFFD97706), CircleShape)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "DEMO DATA MODE — Synthetic records isolated from production evidence",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = DemoBadgeText
          )
        }
        if (onToggleDemoMode != null) {
          Text(
            text = "Switch to Real",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppleBlue,
            modifier = Modifier.clickable { onToggleDemoMode() }
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        if (onBackClick != null) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.White)
              .border(1.dp, AppleBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TextPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
        }

        Column {
          Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
          )
          if (subtitle != null) {
            Text(
              text = subtitle,
              fontSize = 13.sp,
              color = TextSecondary
            )
          }
        }
      }

      // Status Pills on the right
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Local Mode indicator (as requested: "Without backend configuration display: 'Local mode'")
        Box(
          modifier = Modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(100.dp))
            .border(1.dp, AppleBorder, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF0D9488), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Local mode",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
          }
        }

        // Demo Toggle pill if not already in demo mode
        if (!isDemoMode && onToggleDemoMode != null) {
          Box(
            modifier = Modifier
              .clickable { onToggleDemoMode() }
              .background(Color.White, RoundedCornerShape(100.dp))
              .border(1.dp, AppleBorder, RoundedCornerShape(100.dp))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Try Demo",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = AppleBlue
            )
          }
        }
      }
    }
  }
}
