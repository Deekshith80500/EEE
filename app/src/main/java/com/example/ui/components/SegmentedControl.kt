package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleSurfaceLight
import com.example.ui.theme.AppleSurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SegmentedControl(
  items: List<String>,
  selectedIndex: Int,
  onItemSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(AppleSurfaceSubtle)
      .padding(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    items.forEachIndexed { index, title ->
      val isSelected = index == selectedIndex
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(11.dp))
          .background(if (isSelected) AppleSurfaceLight else Color.Transparent)
          .clickable { onItemSelected(index) }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          color = if (isSelected) TextPrimary else TextMuted,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
