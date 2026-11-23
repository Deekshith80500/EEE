package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MetricCard(
  title: String,
  primaryValue: String,
  subtitle: String,
  icon: ImageVector? = null,
  accentColor: Color = AppleBlue,
  isAvailable: Boolean = true,
  onProvenanceClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  AppleCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (icon != null) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.8.sp
          )
        }

        if (onProvenanceClick != null && isAvailable) {
          IconButton(
            onClick = onProvenanceClick,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = "View provenance",
              tint = TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = primaryValue,
        fontSize = if (isAvailable) 28.sp else 16.sp,
        fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Medium,
        color = if (isAvailable) TextPrimary else TextSecondary,
        lineHeight = if (isAvailable) 34.sp else 22.sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = subtitle,
        fontSize = 13.sp,
        color = TextSecondary,
        lineHeight = 18.sp
      )
    }
  }
}
