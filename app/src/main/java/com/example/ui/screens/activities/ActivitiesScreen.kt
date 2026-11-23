package com.example.ui.screens.activities

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.example.core.model.ActivityStatus
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SegmentedControl
import com.example.ui.screens.home.ActivityItemCard
import com.example.ui.theme.AppleBgLight
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActivitiesScreen(
  viewModel: ActivitiesViewModel,
  onNavigateToActivityDetail: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = AppleBgLight,
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.setCreateDialogVisible(true) },
        containerColor = AppleBlue,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_create_activity")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add activity")
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Activity", fontWeight = FontWeight.SemiBold)
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
        title = "Activities",
        subtitle = "Structured Mentoring Sessions",
        isDemoMode = state.isDemoMode
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
      ) {
        SegmentedControl(
          items = listOf("All", "Upcoming / Active", "Completed"),
          selectedIndex = state.filterIndex,
          onItemSelected = { viewModel.setFilter(it) }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (state.allActivities.isEmpty()) {
        val emptyMessage = when (state.filterIndex) {
          1 -> "No upcoming or active mentoring activities scheduled."
          2 -> "No completed activities on record."
          else -> "No activities added to this program yet. Tap '+ New Activity' to structure your first mentoring session."
        }
        EmptyStateView(
          title = "No activities found",
          message = emptyMessage,
          actionLabel = "Create Activity",
          onActionClick = { viewModel.setCreateDialogVisible(true) }
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(state.allActivities, key = { it.id }) { activity ->
            ActivityItemCard(
              activity = activity,
              onClick = { onNavigateToActivityDetail(activity.id) },
              modifier = Modifier.testTag("activity_card_${activity.id}")
            )
          }

          item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
          }
        }
      }
    }

    if (state.showCreateDialog) {
      CreateActivityDialog(
        onDismiss = { viewModel.setCreateDialogVisible(false) },
        onConfirm = { title, desc, duration, mentor, location, status ->
          viewModel.createActivity(title, desc, duration, mentor, location, status)
        }
      )
    }
  }
}

@Composable
fun CreateActivityDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, Int, String, String, ActivityStatus) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var mentorName by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }
  var durationStr by remember { mutableStateOf("60") }
  var selectedStatus by remember { mutableStateOf(ActivityStatus.SCHEDULED) }
  var isError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "New Mentoring Activity",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it; isError = false },
          label = { Text("Activity Title *") },
          placeholder = { Text("e.g. Session 1: Mentorship Orientation") },
          isError = isError && title.isBlank(),
          modifier = Modifier.fillMaxWidth().testTag("input_activity_title"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppleBorder,
            focusedBorderColor = AppleBlue
          )
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Objective / Description") },
          placeholder = { Text("Brief outline of session goals") },
          modifier = Modifier.fillMaxWidth().testTag("input_activity_desc"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppleBorder,
            focusedBorderColor = AppleBlue
          )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = mentorName,
            onValueChange = { mentorName = it },
            label = { Text("Mentor Name") },
            placeholder = { Text("e.g. David") },
            modifier = Modifier.weight(1f).testTag("input_mentor_name"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = AppleBorder,
              focusedBorderColor = AppleBlue
            )
          )

          OutlinedTextField(
            value = durationStr,
            onValueChange = { durationStr = it },
            label = { Text("Duration (min)") },
            modifier = Modifier.weight(1f).testTag("input_duration"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = AppleBorder,
              focusedBorderColor = AppleBlue
            )
          )
        }

        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text("Location / Medium") },
          placeholder = { Text("Room 2B or Virtual Call") },
          modifier = Modifier.fillMaxWidth().testTag("input_location"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppleBorder,
            focusedBorderColor = AppleBlue
          )
        )

        // Status Segmented control
        Column {
          Text(text = "Initial Status", fontSize = 12.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          val statuses = listOf(ActivityStatus.SCHEDULED, ActivityStatus.ACTIVE, ActivityStatus.DRAFT)
          val selectedIndex = statuses.indexOf(selectedStatus).coerceAtLeast(0)
          SegmentedControl(
            items = statuses.map { it.label },
            selectedIndex = selectedIndex,
            onItemSelected = { selectedStatus = statuses[it] }
          )
        }

        if (isError) {
          Text(
            text = "Please enter an activity title.",
            color = Color(0xFFDC2626),
            fontSize = 12.sp
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isBlank()) {
            isError = true
          } else {
            val duration = durationStr.toIntOrNull() ?: 60
            onConfirm(title, description, duration, mentorName, location, selectedStatus)
          }
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
        modifier = Modifier.testTag("btn_save_activity")
      ) {
        Text("Save Activity", color = Color.White, fontWeight = FontWeight.SemiBold)
      }
    },
    dismissButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
      ) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
