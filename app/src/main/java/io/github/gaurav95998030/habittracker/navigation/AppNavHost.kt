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
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StoryItem
import io.github.gaurav95998030.habittracker.features.motivation.presentation.CheckInScreen
import io.github.gaurav95998030.habittracker.features.motivation.presentation.MissPatternsScreen
import io.github.gaurav95998030.habittracker.features.motivation.presentation.MotivationViewModel
import io.github.gaurav95998030.habittracker.features.motivation.presentation.PlansScreen
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StackEditorSheet
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StackScreen
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitDraft
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.gaurav95998030.habittracker.features.reminders.presentation.NotificationCenterScreen
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsScreen
import io.github.gaurav95998030.habittracker.features.settings.presentation.PrivacyPolicyScreen
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion

/** Non-tab detail route reachable from the Home header bell and Settings. */
const val NOTIFICATIONS_ROUTE = "notifications"
const val PLANS_ROUTE = "plans"
const val MISSES_ROUTE = "misses"
const val CHECKIN_ROUTE = "checkin"
const val PRIVACY_ROUTE = "privacy"
private const val STACK_ROUTE = "stack/{stackId}"
fun stackRoute(id: String) = "stack/$id"

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
    onOpenStory: (StoryItem) -> Unit,
    onRequestLockScreen: () -> Unit,
    motivationViewModel: MotivationViewModel,
    onOpenMissSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userName by settingsViewModel.userName.collectAsStateWithLifecycle()
    val focusCategories by settingsViewModel.focusCategories.collectAsStateWithLifecycle()
    val seenStories by settingsViewModel.seenStories.collectAsStateWithLifecycle()
    val habitState by habitViewModel.uiState.collectAsStateWithLifecycle()
    val habits = habitState.habits.orEmpty()
    val stacks by motivationViewModel.stacks.collectAsStateWithLifecycle()
    val missLogs by motivationViewModel.missLogs.collectAsStateWithLifecycle()
    val energy by motivationViewModel.energy.collectAsStateWithLifecycle()
    val activePlans by motivationViewModel.activePlans.collectAsStateWithLifecycle()
    val plans by motivationViewModel.plans.collectAsStateWithLifecycle()

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
                seenStories = seenStories,
                onOpenStory = onOpenStory,
                stacks = stacks,
                onOpenStack = { navController.navigate(stackRoute(it)) },
                onCreateStack = { name, steps -> motivationViewModel.saveStack(null, name, steps) },
                onOpenPlans = { navController.navigate(PLANS_ROUTE) },
                onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
                onOpenSettings = { onSelectTab(TopLevelDestination.Settings) },
            )
        }
        composable(TopLevelDestination.Insights.route) {
            InsightsScreen(
                viewModel = habitViewModel,
                onOpenYearReview = onOpenStory,
                missLogs = missLogs,
                energy = energy,
                onSetEnergy = motivationViewModel::setTodayEnergy,
                onOpenMisses = { navController.navigate(MISSES_ROUTE) },
                onOpenCheckIn = { navController.navigate(CHECKIN_ROUTE) },
            )
        }
        composable(TopLevelDestination.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
                onOpenWidget = onOpenWidgetPromo,
                onOpenPrivacy = { navController.navigate(PRIVACY_ROUTE) },
                onRequestLockScreen = onRequestLockScreen,
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
        composable(PLANS_ROUTE) {
            PlansScreen(
                plans = plans,
                activePlans = activePlans,
                onStart = { plan, chosen ->
                    habitViewModel.addHabits(chosen.map { h ->
                        HabitDraft(h.name, h.category, Accents.storageHex(h.colorHex), h.icon)
                    })
                    motivationViewModel.startPlan(plan.id)
                },
                onEnd = { motivationViewModel.endPlan(it.id) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(MISSES_ROUTE) {
            MissPatternsScreen(
                habits = habits,
                logs = missLogs,
                onBack = { navController.popBackStack() },
                onLogYesterday = onOpenMissSheet,
                onEditHabit = { h ->
                    habitViewModel.requestEditHabit(h.id)
                    navController.popBackStack()
                    onSelectTab(TopLevelDestination.Home)
                },
            )
        }
        composable(PRIVACY_ROUTE) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        composable(CHECKIN_ROUTE) {
            CheckInScreen(habits, energy, motivationViewModel::setTodayEnergy, onBack = { navController.popBackStack() })
        }
        composable(STACK_ROUTE) { entry ->
            val id = entry.arguments?.getString("stackId")
            val stack = stacks.firstOrNull { it.id == id }
            var editing by rememberSaveable { mutableStateOf(false) }
            if (stack == null) {
                // Freshly created (DB write in flight) or deleted — nothing to show yet.
                Box(Modifier.fillMaxSize().background(AppTheme.colors.background))
            } else {
                StackScreen(
                    stack = stack,
                    habits = habits,
                    onCompleteStep = { h -> habitViewModel.toggleToday(h.id) },
                    onEdit = { editing = true },
                    onBack = { navController.popBackStack() },
                )
                if (editing) {
                    StackEditorSheet(
                        initial = stack,
                        habits = habits,
                        onSave = { name, steps ->
                            motivationViewModel.saveStack(stack.id, name, steps)
                            editing = false
                        },
                        onDelete = {
                            editing = false
                            motivationViewModel.deleteStack(stack.id)
                            navController.popBackStack()
                        },
                        onDismiss = { editing = false },
                    )
                }
            }
        }
    }
}
