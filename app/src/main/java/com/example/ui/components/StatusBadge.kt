package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ActivityStatus
import com.example.core.model.ParticipationStatus
import com.example.ui.theme.DemoBadgeBg
import com.example.ui.theme.DemoBadgeBorder
import com.example.ui.theme.DemoBadgeText
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusNeutralBg
import com.example.ui.theme.StatusNeutralText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText

@Composable
fun StatusBadge(
  text: String,
  backgroundColor: Color,
  textColor: Color,
  borderColor: Color? = null,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .then(
        if (borderColor != null) {
          Modifier.border(1.dp, borderColor, RoundedCornerShape(100.dp))
        } else {
          Modifier
        }
      )
      .background(backgroundColor, RoundedCornerShape(100.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      color = textColor,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.2.sp
    )
  }
}

@Composable
fun ParticipationBadge(status: ParticipationStatus, modifier: Modifier = Modifier) {
  when (status) {
    ParticipationStatus.PARTICIPATED -> StatusBadge(
      text = "Participated",
      backgroundColor = StatusSuccessBg,
      textColor = StatusSuccessText,
      modifier = modifier
    )
    ParticipationStatus.DID_NOT_PARTICIPATE -> StatusBadge(
      text = "Did not participate",
      backgroundColor = StatusErrorBg,
      textColor = StatusErrorText,
      modifier = modifier
    )
    ParticipationStatus.NOT_RECORDED -> StatusBadge(
      text = "Not recorded",
      backgroundColor = StatusNeutralBg,
      textColor = StatusNeutralText,
      modifier = modifier
    )
  }
}

@Composable
fun ActivityStatusBadge(status: String, modifier: Modifier = Modifier) {
  when (status.uppercase()) {
    ActivityStatus.COMPLETED.name -> StatusBadge(
      text = "Completed",
      backgroundColor = StatusSuccessBg,
      textColor = StatusSuccessText,
      modifier = modifier
    )
    ActivityStatus.ACTIVE.name -> StatusBadge(
      text = "Active",
      backgroundColor = Color(0xFFE0F2FE),
      textColor = Color(0xFF0369A1),
      modifier = modifier
    )
    ActivityStatus.SCHEDULED.name -> StatusBadge(
      text = "Scheduled",
      backgroundColor = StatusWarningBg,
      textColor = StatusWarningText,
      modifier = modifier
    )
    else -> StatusBadge(
      text = "Draft",
      backgroundColor = StatusNeutralBg,
      textColor = StatusNeutralText,
      modifier = modifier
    )
  }
}

@Composable
fun DemoDataBadge(modifier: Modifier = Modifier) {
  StatusBadge(
    text = "Demo Data",
    backgroundColor = DemoBadgeBg,
    textColor = DemoBadgeText,
    borderColor = DemoBadgeBorder,
    modifier = modifier
  )
}
