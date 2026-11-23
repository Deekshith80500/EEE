package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorderSubtle
import com.example.ui.theme.AppleSurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class ProvenanceDetails(
  val metricName: String,
  val valueDisplayed: String,
  val sourceClassification: String, // REAL, DEMO, DERIVED
  val calculationMethod: String,
  val denominatorDefinition: String? = null,
  val verifiedRecordCount: Int,
  val measurementDefinition: String,
  val timestampOrPeriod: String,
  val integrityNotes: String? = null
)

@Composable
fun ProvenanceDialog(
  details: ProvenanceDetails,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = AppleSurfaceLight),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Provenance",
              tint = AppleBlue
            )
            Text(
              text = " Data Provenance",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Value block
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Text(text = details.metricName, fontSize = 13.sp, color = TextSecondary)
          Text(
            text = details.valueDisplayed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Classification: ${details.sourceClassification}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (details.sourceClassification == "DEMO") Color(0xFFD97706) else AppleBlue
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProvenanceField("Calculation Method", details.calculationMethod)
        details.denominatorDefinition?.let {
          ProvenanceField("Explicit Denominator", it)
        }
        ProvenanceField("Verified Source Records", "${details.verifiedRecordCount} records")
        ProvenanceField("Measurement Definition", details.measurementDefinition)
        ProvenanceField("Evaluation Period / Time", details.timestampOrPeriod)

        details.integrityNotes?.let {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Data Integrity Guarantee:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F766E)
          )
          Text(
            text = it,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
        ) {
          Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun ProvenanceField(label: String, value: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(
      text = label.uppercase(),
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = TextMuted,
      letterSpacing = 0.5.sp
    )
    Text(
      text = value,
      fontSize = 13.sp,
      color = TextPrimary
    )
  }
}
