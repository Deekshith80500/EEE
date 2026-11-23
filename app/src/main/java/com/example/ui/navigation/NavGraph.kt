package com.example.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.YouthMonitorApplication
import com.example.ui.screens.activities.ActivitiesScreen
import com.example.ui.screens.activities.ActivitiesViewModel
import com.example.ui.screens.activities.ActivityDetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.insights.InsightsViewModel
import com.example.ui.screens.monitoring.MonitoringScreen
import com.example.ui.screens.monitoring.MonitoringViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBorderSubtle
import com.example.ui.theme.AppleSurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MainAppNavigation(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  val context = LocalContext.current.applicationContext as YouthMonitorApplication
  val repository = context.repository

  val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
  val activitiesViewModel: ActivitiesViewModel = viewModel(factory = ActivitiesViewModel.Factory(repository))
  val monitoringViewModel: MonitoringViewModel = viewModel(factory = MonitoringViewModel.Factory(repository))
  val insightsViewModel: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory(repository))
  val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository))

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val isDetailScreen = currentRoute?.startsWith("activity_detail") == true

  Scaffold(
    modifier = modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0.dp),
    bottomBar = {
      if (!isDetailScreen) {
        NavigationBar(
          containerColor = AppleSurfaceLight,
          windowInsets = WindowInsets.navigationBars,
          tonalElevation = 0.dp
        ) {
          Screen.bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
              selected = isSelected,
              onClick = {
                if (currentRoute != screen.route) {
                  navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                      saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                  }
                }
              },
              icon = {
                Icon(
                  imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                  contentDescription = screen.title
                )
              },
              label = {
                Text(
                  text = screen.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleBlue,
                selectedTextColor = AppleBlue,
                indicatorColor = Color(0xFFE0F2FE),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
              )
            )
          }
        }
      }
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Screen.Home.route,
      modifier = Modifier.padding(innerPadding)
    ) {
      composable(Screen.Home.route) {
        HomeScreen(
          viewModel = homeViewModel,
          onNavigateToActivityDetail = { activityId ->
            navController.navigate("activity_detail/$activityId")
          },
          onNavigateToActivities = {
            navController.navigate(Screen.Activities.route)
          },
          onNavigateToMonitoring = {
            navController.navigate(Screen.Monitoring.route)
          }
        )
      }

      composable(Screen.Activities.route) {
        ActivitiesScreen(
          viewModel = activitiesViewModel,
          onNavigateToActivityDetail = { activityId ->
            navController.navigate("activity_detail/$activityId")
          }
        )
      }

      composable(
        route = "activity_detail/{activityId}",
        arguments = listOf(navArgument("activityId") { type = NavType.StringType })
      ) { backStackEntry ->
        val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
        val activityFlow = repository.getActivityById(activityId)
        val activity by activityFlow.collectAsStateWithLifecycle(initialValue = null)

        val isDemo = repository.isDemoMode.collectAsStateWithLifecycle()
        val participants by repository.getParticipants(isDemo.value).collectAsStateWithLifecycle(initialValue = emptyList())
        val participations by repository.getParticipationsByActivity(activityId).collectAsStateWithLifecycle(initialValue = emptyList())
        val activeDefs by repository.getActiveEngagementDefinitions().collectAsStateWithLifecycle(initialValue = emptyList())
        val engagementRecords by repository.getEngagementRecordsByActivity(activityId).collectAsStateWithLifecycle(initialValue = emptyList())
        val assessments by repository.getAssessmentsByActivity(activityId).collectAsStateWithLifecycle(initialValue = emptyList())
        val allResults by repository.getAllAssessmentResults(isDemo.value).collectAsStateWithLifecycle(initialValue = emptyList())

        ActivityDetailScreen(
          activity = activity,
          participants = participants,
          participations = participations,
          activeEngagementDef = activeDefs.firstOrNull(),
          engagementRecords = engagementRecords,
          assessments = assessments,
          assessmentResults = allResults,
          onBackClick = { navController.popBackStack() },
          onRecordParticipation = { pId, status ->
            activitiesViewModel.recordParticipation(activityId, pId, status)
          },
          onRecordEngagement = { pId, defId, score, notes ->
            activitiesViewModel.recordEngagement(activityId, pId, defId, score, notes)
          },
          onRecordAssessmentScore = { assId, pId, score ->
            activitiesViewModel.recordAssessmentScore(assId, pId, score)
          },
          onUpdateStatus = { newStatus ->
            activity?.let { activitiesViewModel.updateActivityStatus(it, newStatus) }
          },
          onDeleteActivity = {
            activitiesViewModel.deleteActivity(activityId)
            navController.popBackStack()
          }
        )
      }

      composable(Screen.Monitoring.route) {
        MonitoringScreen(viewModel = monitoringViewModel)
      }

      composable(Screen.Insights.route) {
        InsightsScreen(viewModel = insightsViewModel)
      }

      composable(Screen.Settings.route) {
        SettingsScreen(viewModel = settingsViewModel)
      }
    }
  }
}
