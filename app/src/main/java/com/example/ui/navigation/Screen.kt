package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
  val route: String,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  object Home : Screen(
    route = "home",
    title = "Home",
    selectedIcon = Icons.Filled.Dashboard,
    unselectedIcon = Icons.Outlined.Dashboard
  )

  object Activities : Screen(
    route = "activities",
    title = "Activities",
    selectedIcon = Icons.Filled.CalendarMonth,
    unselectedIcon = Icons.Outlined.CalendarMonth
  )

  object Monitoring : Screen(
    route = "monitoring",
    title = "Monitoring",
    selectedIcon = Icons.AutoMirrored.Outlined.FactCheck,
    unselectedIcon = Icons.AutoMirrored.Outlined.FactCheck
  )

  object Insights : Screen(
    route = "insights",
    title = "Insights",
    selectedIcon = Icons.Filled.Assessment,
    unselectedIcon = Icons.Outlined.Assessment
  )

  object Settings : Screen(
    route = "settings",
    title = "Settings",
    selectedIcon = Icons.Filled.Settings,
    unselectedIcon = Icons.Outlined.Settings
  )

  companion object {
    val bottomNavItems = listOf(Home, Activities, Monitoring, Insights, Settings)
  }
}
