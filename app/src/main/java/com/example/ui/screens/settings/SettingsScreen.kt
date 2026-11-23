package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppleCard
import com.example.ui.components.AppTopBar
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorder
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.DemoBadgeBg
import com.example.ui.theme.DemoBadgeBorder
import com.example.ui.theme.DemoBadgeText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppleBgLight)
  ) {
    AppTopBar(
      title = "Settings",
      subtitle = "Configuration & Integrity",
      isDemoMode = state.isDemoMode
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Feedback snackbar banner
      state.feedbackMessage?.let { msg ->
        item {
          AppleCard(
            backgroundColor = Color(0xFFF0FDF4),
            borderColor = Color(0xFFBBF7D0),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = msg, fontSize = 13.sp, color = Color(0xFF166534), modifier = Modifier.weight(1f))
              IconButton(onClick = { viewModel.dismissFeedback() }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // 1. PROGRAM SPECIFICATION
      item {
        AppleCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "PROGRAM CONFIGURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 0.8.sp
              )
              IconButton(
                onClick = { viewModel.setEditProgramVisible(true) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Program", tint = AppleBlue, modifier = Modifier.size(16.dp))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = state.program?.name ?: "Youth Mentoring Program",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            if (state.program?.description?.isNotBlank() == true) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = state.program!!.description,
                fontSize = 13.sp,
                color = TextSecondary
              )
            }

            if (state.program?.organization?.isNotBlank() == true) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Organization: ${state.program!!.organization}",
                fontSize = 12.sp,
                color = TextMuted
              )
            }
          }
        }
      }

      // 2. DATA MODE & DEMO MANAGEMENT
      item {
        AppleCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "DATA ISOLATION & DEMO MODE",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextMuted,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Demo Data Mode",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextPrimary
                )
                Text(
                  text = "Populates isolated synthetic records (seed: 42) for UI preview without contaminating production data.",
                  fontSize = 12.sp,
                  color = TextSecondary,
                  lineHeight = 16.sp
                )
              }
              Switch(
                checked = state.isDemoMode,
                onCheckedChange = { viewModel.toggleDemoMode() },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = AppleBlue
                ),
                modifier = Modifier.testTag("switch_demo_mode")
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = { viewModel.loadDemoData() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Seed Demo", color = AppleBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }

              Button(
                onClick = { viewModel.clearDemoData() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFBEB)),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear Demo", color = Color(0xFFD97706), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // 3. STORAGE & SYNC ARCHITECTURE
      item {
        AppleCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "STORAGE & PERSISTENCE ARCHITECTURE",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextMuted,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(Color(0xFF0D9488), CircleShape)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Local Mode Active (Room SQLite Database)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "The application functions completely offline-first. Remote backend synchronization is not configured, so all evidence is stored securely and exclusively on this device.",
              fontSize = 12.sp,
              color = TextSecondary,
              lineHeight = 17.sp
            )
          }
        }
      }

      // 4. PRIVACY & DATA MINIMIZATION
      item {
        AppleCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = AppleTeal, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "PRIVACY & DATA MINIMIZATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 0.8.sp
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "In compliance with program privacy rules:\n• No personal government IDs (Aadhaar, PAN, SSN) are collected.\n• No phone numbers, home addresses, or religious demographic fields.\n• Participants are abstracted using minimal display identifiers.\n• No metrics or outcome claims are fabricated.",
              fontSize = 12.sp,
              color = TextSecondary,
              lineHeight = 18.sp
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (state.showEditProgramDialog) {
      EditProgramDialog(
        currentName = state.program?.name ?: "Youth Mentoring Program",
        currentDesc = state.program?.description ?: "",
        currentOrg = state.program?.organization ?: "",
        onDismiss = { viewModel.setEditProgramVisible(false) },
        onConfirm = { name, desc, org ->
          viewModel.saveProgram(name, desc, org)
        }
      )
    }
  }
}

@Composable
fun EditProgramDialog(
  currentName: String,
  currentDesc: String,
  currentOrg: String,
  onDismiss: () -> Unit,
  onConfirm: (name: String, desc: String, org: String) -> Unit
) {
  var name by remember { mutableStateOf(currentName) }
  var desc by remember { mutableStateOf(currentDesc) }
  var org by remember { mutableStateOf(currentOrg) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = {
      Text("Configure Program", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Program Title") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
          value = org,
          onValueChange = { org = it },
          label = { Text("Organization") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, desc, org) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
      ) {
        Text("Save", color = Color.White)
      }
    },
    dismissButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
