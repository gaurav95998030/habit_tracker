package io.github.gaurav95998030.habittracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.HabitListScreen
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.insights.presentation.InsightsScreen
import io.github.gaurav95998030.habittracker.features.reminders.presentation.NotificationCenterScreen
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsScreen
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion

/** Non-tab detail route reachable from the Home header bell and Settings. */
const val NOTIFICATIONS_ROUTE = "notifications"

/**
 * App-wide NavHost. Each top-level destination is one composable route.
 *
 * habitViewModel and settingsViewModel are supplied by the host (MainActivity) so
 * deep-link events delivered to the Activity (e.g. notification taps invoking
 * viewModel.openHabitDetail) reach the same instance the UI is observing.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    habitViewModel: HabitViewModel,
    settingsViewModel: SettingsViewModel,
    onSelectTab: (TopLevelDestination) -> Unit,
    onOpenWidgetPromo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userName by settingsViewModel.userName.collectAsStateWithLifecycle()
    val focusCategories by settingsViewModel.focusCategories.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Start.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(300, easing = AppMotion.emphasized)) },
        exitTransition = { fadeOut(tween(200)) },
    ) {
        composable(TopLevelDestination.Home.route) {
            HabitListScreen(
                viewModel = habitViewModel,
                userName = userName,
                focusCategories = focusCategories,
                onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
                onOpenSettings = { onSelectTab(TopLevelDestination.Settings) },
            )
        }
        composable(TopLevelDestination.Insights.route) {
            InsightsScreen(viewModel = habitViewModel)
        }
        composable(TopLevelDestination.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
                onOpenWidget = onOpenWidgetPromo,
            )
        }
        composable(NOTIFICATIONS_ROUTE) {
            NotificationCenterScreen(
                viewModel = habitViewModel,
                userName = userName,
                onBack = { navController.popBackStack() },
                onOpenHabit = { habitId ->
                    // Detail lives on Home: hand the id to the shared VM, then go there.
                    habitViewModel.openHabitDetail(habitId)
                    navController.popBackStack()
                    onSelectTab(TopLevelDestination.Home)
                },
            )
        }
    }
}
