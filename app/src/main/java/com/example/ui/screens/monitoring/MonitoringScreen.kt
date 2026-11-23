package com.example.ui.screens.monitoring

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.IndicatorEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.ui.components.AppleCard
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SegmentedControl
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorder
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MonitoringScreen(
  viewModel: MonitoringViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val tabs = listOf("Participants", "Engagement Setup", "Indicators")

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = AppleBgLight,
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          when (state.selectedTab) {
            0 -> viewModel.setAddParticipantVisible(true)
            1 -> viewModel.setAddEngagementVisible(true)
            2 -> viewModel.setAddIndicatorVisible(true)
          }
        },
        containerColor = AppleBlue,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_monitoring_add")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add")
          Spacer(modifier = Modifier.width(6.dp))
          val label = when (state.selectedTab) {
            0 -> "Add Participant"
            1 -> "Define Method"
            else -> "Add Indicator"
          }
          Text(label, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AppTopBar(
        title = "Monitoring",
        subtitle = "Operations & Configuration",
        isDemoMode = state.isDemoMode
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
      ) {
        SegmentedControl(
          items = tabs,
          selectedIndex = state.selectedTab,
          onItemSelected = { viewModel.setSelectedTab(it) }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      when (state.selectedTab) {
        0 -> ParticipantsListTab(
          participants = state.participants,
          onAddClick = { viewModel.setAddParticipantVisible(true) }
        )
        1 -> EngagementConfigTab(
          definitions = state.engagementDefinitions,
          onAddClick = { viewModel.setAddEngagementVisible(true) }
        )
        2 -> IndicatorsConfigTab(
          indicators = state.indicators,
          onAddClick = { viewModel.setAddIndicatorVisible(true) }
        )
      }
    }

    if (state.showAddParticipantDialog) {
      AddParticipantDialog(
        onDismiss = { viewModel.setAddParticipantVisible(false) },
        onConfirm = { name, ref, cohort ->
          viewModel.addParticipant(name, ref, cohort)
        }
      )
    }

    if (state.showAddEngagementDialog) {
      AddEngagementDefinitionDialog(
        onDismiss = { viewModel.setAddEngagementVisible(false) },
        onConfirm = { name, method, min, max, unit, desc ->
          viewModel.saveEngagementDefinition(name, method, min, max, unit, desc)
        }
      )
    }

    if (state.showAddIndicatorDialog) {
      AddIndicatorDialog(
        onDismiss = { viewModel.setAddIndicatorVisible(false) },
        onConfirm = { name, cat, def, unit, method ->
          viewModel.saveIndicator(name, cat, def, unit, method)
        }
      )
    }
  }
}

@Composable
private fun ParticipantsListTab(
  participants: List<ParticipantEntity>,
  onAddClick: () -> Unit
) {
  if (participants.isEmpty()) {
    EmptyStateView(
      title = "No participants registered",
      message = "Register youth participants to enable attendance tracking and skill assessments.",
      actionLabel = "Add Participant",
      onActionClick = onAddClick
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
      AppleCard(backgroundColor = Color(0xFFF1F5F9), modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Group, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Total Registered: ${participants.size} participants. Identities are protected with minimal necessary fields.",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }

    items(participants, key = { it.id }) { p ->
      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFE0F2FE), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = p.displayName.take(2).uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppleBlue
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = p.displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Text(
                text = listOfNotNull(p.externalReference, p.groupCohort).joinToString(" • ").ifBlank { "Registered Participant" },
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }

          if (p.sourceType == "DEMO") {
            Text(text = "Demo", fontSize = 11.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun EngagementConfigTab(
  definitions: List<EngagementDefinitionEntity>,
  onAddClick: () -> Unit
) {
  if (definitions.isEmpty()) {
    EmptyStateView(
      title = "Engagement measurement method not configured",
      message = "Without a defined measurement method, the application will not calculate or assume engagement scores.",
      actionLabel = "Define Method",
      onActionClick = onAddClick
    )
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(definitions, key = { it.id }) { def ->
      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = def.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            if (def.active) {
              Box(
                modifier = Modifier
                  .background(Color(0xFFD1FAE5), RoundedCornerShape(100.dp))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (def.description.isNotBlank()) {
            Text(text = def.description, fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Column {
              Text("SCALE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
              Text("${def.scaleMin.toInt()} to ${def.scaleMax.toInt()} ${def.unit}", fontSize = 12.sp, color = TextPrimary)
            }
            Column {
              Text("METHODOLOGY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
              Text(def.methodology, fontSize = 12.sp, color = TextPrimary)
            }
            Column {
              Text("VERSION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
              Text("v${def.version}", fontSize = 12.sp, color = TextPrimary)
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun IndicatorsConfigTab(
  indicators: List<IndicatorEntity>,
  onAddClick: () -> Unit
) {
  if (indicators.isEmpty()) {
    EmptyStateView(
      title = "No skill / awareness indicators",
      message = "Define measurable competencies or awareness indicators to evaluate mentoring outcomes.",
      actionLabel = "Add Indicator",
      onActionClick = onAddClick
    )
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(indicators, key = { it.id }) { ind ->
      AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = ind.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Box(
              modifier = Modifier
                .background(Color(0xFFEEF2FF), RoundedCornerShape(100.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(ind.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          if (ind.definition.isNotBlank()) {
            Text(text = ind.definition, fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
          }

          Text(
            text = "Method: ${ind.measurementMethod} • Unit: ${ind.unit}",
            fontSize = 12.sp,
            color = TextMuted
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
fun AddParticipantDialog(
  onDismiss: () -> Unit,
  onConfirm: (displayName: String, ref: String?, cohort: String?) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var ref by remember { mutableStateOf("") }
  var cohort by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = {
      Text("Add Participant", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it; isError = false },
          label = { Text("Display Name / Code *") },
          placeholder = { Text("e.g. Participant 01 or initials") },
          isError = isError,
          modifier = Modifier.fillMaxWidth().testTag("input_participant_name"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppleBorder,
            focusedBorderColor = AppleBlue
          )
        )

        OutlinedTextField(
          value = ref,
          onValueChange = { ref = it },
          label = { Text("External Reference (Optional)") },
          placeholder = { Text("e.g. REF-2024") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppleBorder,
            focusedBorderColor = AppleBlue
          )
        )

        OutlinedTextField(
          value = cohort,
          onValueChange = { cohort = it },
          label = { Text("Cohort / Group (Optional)") },
          placeholder = { Text("e.g. Cohort Alpha") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppleBorder,
            focusedBorderColor = AppleBlue
          )
        )

        Text(
          text = "Data Minimization: We do not collect government IDs, sensitive personal details, or contact data.",
          fontSize = 11.sp,
          color = TextMuted,
          lineHeight = 15.sp
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            isError = true
          } else {
            onConfirm(name, ref, cohort)
          }
        },
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

@Composable
fun AddEngagementDefinitionDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, method: String, min: Double, max: Double, unit: String, desc: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var method by remember { mutableStateOf("OBSERVATION_RUBRIC") }
  var minStr by remember { mutableStateOf("1") }
  var maxStr by remember { mutableStateOf("5") }
  var unit by remember { mutableStateOf("scale (1-5)") }
  var desc by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = {
      Text("Configure Engagement Method", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Method Name *") },
          placeholder = { Text("e.g. Structured Session Rubric") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description / Protocol") },
          placeholder = { Text("e.g. Attentiveness and dialogue observation") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = minStr,
            onValueChange = { minStr = it },
            label = { Text("Min Scale") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = maxStr,
            onValueChange = { maxStr = it },
            label = { Text("Max Scale") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
        }

        OutlinedTextField(
          value = unit,
          onValueChange = { unit = it },
          label = { Text("Unit Label") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(
              name,
              method,
              minStr.toDoubleOrNull() ?: 1.0,
              maxStr.toDoubleOrNull() ?: 5.0,
              unit,
              desc
            )
          }
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
      ) {
        Text("Save Method", color = Color.White)
      }
    },
    dismissButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}

@Composable
fun AddIndicatorDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, cat: String, def: String, unit: String, method: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("SKILL") }
  var definition by remember { mutableStateOf("") }
  var unit by remember { mutableStateOf("score points (0-100)") }
  var method by remember { mutableStateOf("Pre/Post Assessment") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = {
      Text("Add Skill / Awareness Indicator", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Indicator Name *") },
          placeholder = { Text("e.g. Budgeting Confidence") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          val isSkill = category == "SKILL"
          Button(
            onClick = { category = "SKILL" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSkill) AppleBlue else Color(0xFFF1F5F9)
            ),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Skill", color = if (isSkill) Color.White else TextPrimary)
          }
          Button(
            onClick = { category = "AWARENESS" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (!isSkill) AppleBlue else Color(0xFFF1F5F9)
            ),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Awareness", color = if (!isSkill) Color.White else TextPrimary)
          }
        }

        OutlinedTextField(
          value = definition,
          onValueChange = { definition = it },
          label = { Text("Definition") },
          placeholder = { Text("Specific ability or knowledge measured") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
          value = method,
          onValueChange = { method = it },
          label = { Text("Measurement Method") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(name, category, definition, unit, method)
          }
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
      ) {
        Text("Save Indicator", color = Color.White)
      }
    },
    dismissButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
