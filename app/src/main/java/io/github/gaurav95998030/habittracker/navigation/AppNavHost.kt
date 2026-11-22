package io.github.gaurav95998030.habittracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.HabitListScreen
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.insights.presentation.InsightsScreen
import io.github.gaurav95998030.habittracker.features.reminders.presentation.NotificationCenterScreen
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsScreen

/** Non-tab detail route reachable from the Home header bell and Settings. */
const val NOTIFICATIONS_ROUTE = "notifications"

/**
 * App-wide NavHost. Each top-level destination is one composable route.
 *
 * habitViewModel is supplied by the host (MainActivity) so deep-link events
 * delivered to the Activity (e.g. notification taps invoking
 * viewModel.openHabitDetail) reach the same ViewModel instance the UI is
 * observing. If you ever stop needing Activity-scoped state, swap this for
 * `hiltViewModel()` inside the composable block.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    habitViewModel: HabitViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Start.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.Home.route) {
            HabitListScreen(
                viewModel = habitViewModel,
                onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
            )
        }
        composable(TopLevelDestination.Insights.route) {
            InsightsScreen(viewModel = habitViewModel)
        }
        composable(TopLevelDestination.Settings.route) {
            SettingsScreen(
                onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
            )
        }
        composable(NOTIFICATIONS_ROUTE) {
            NotificationCenterScreen(
                viewModel = habitViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
