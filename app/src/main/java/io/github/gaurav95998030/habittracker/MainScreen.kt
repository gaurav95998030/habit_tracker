package io.github.gaurav95998030.habittracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsViewModel
import io.github.gaurav95998030.habittracker.features.motivation.presentation.MotivationStory
import io.github.gaurav95998030.habittracker.features.motivation.presentation.MissLogSheet
import io.github.gaurav95998030.habittracker.features.motivation.presentation.MotivationViewModel
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StoryItem
import io.github.gaurav95998030.habittracker.features.widget.presentation.WidgetPromoSheet
import io.github.gaurav95998030.habittracker.features.widget.presentation.LockScreenConsentSheet
import io.github.gaurav95998030.habittracker.navigation.AppBottomBar
import io.github.gaurav95998030.habittracker.navigation.AppNavHost
import io.github.gaurav95998030.habittracker.navigation.TopLevelDestination
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

/**
 * Root screen of the app (post-onboarding).
 *
 * Owns the NavController, observes the current backstack entry, derives the
 * "selected tab" from it, and handles the navigation transaction when a tab
 * is tapped. The bottom bar is hidden on non-tab routes (Notification center).
 *
 * Also hosts the home-screen widget nudge: shown once automatically (on a launch
 * after onboarding, when there's at least one habit) and on demand from Settings.
 *
 * @param widgetPromoEligible true when this launch started with onboarding already
 * complete, so a brand-new user isn't hit with the nudge right after onboarding.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    habitViewModel: HabitViewModel,
    settingsViewModel: SettingsViewModel,
    motivationViewModel: MotivationViewModel,
    widgetPromoEligible: Boolean,
) {
    val navController = rememberNavController()

    // currentBackStackEntryAsState recomposes when the active destination
    // changes — so the bar's selected state stays in sync with whichever
    // route the NavController is on (e.g. system back button).
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }
    val selected = currentTab ?: TopLevelDestination.Start

    val habitState by habitViewModel.uiState.collectAsStateWithLifecycle()
    val promoSeen by settingsViewModel.widgetPromoSeen.collectAsStateWithLifecycle()
    var promoRequested by rememberSaveable { mutableStateOf(false) }
    var openStory by remember { mutableStateOf<StoryItem?>(null) }
    var lockConsentRequested by rememberSaveable { mutableStateOf(false) }
    val lockPrompted by settingsViewModel.lockScreenPrompted.collectAsStateWithLifecycle()
    val lockEnabled by settingsViewModel.lockScreenEnabled.collectAsStateWithLifecycle()
    val userName by settingsViewModel.userName.collectAsStateWithLifecycle()
    val savedQuotes by settingsViewModel.savedQuotes.collectAsStateWithLifecycle()
    val missSheetRequested by motivationViewModel.missSheetRequested.collectAsStateWithLifecycle()
    val missLogs by motivationViewModel.missLogs.collectAsStateWithLifecycle()
    val habits = habitState.habits.orEmpty()
    val autoPromo = widgetPromoEligible && !promoSeen && !habitState.isLoading &&
        habits.isNotEmpty() && currentTab == TopLevelDestination.Home && habitState.pendingDetailHabitId == null

    val selectTab: (TopLevelDestination) -> Unit = { onTabSelected(navController, it, currentRoute) }

    Scaffold(
        bottomBar = {
            if (currentTab != null) AppBottomBar(selected = selected, onSelect = selectTab)
        },
        containerColor = AppTheme.colors.background,
        // Screens handle the status bar themselves (edge-to-edge headers).
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            AppNavHost(
                navController = navController,
                habitViewModel = habitViewModel,
                settingsViewModel = settingsViewModel,
                onSelectTab = selectTab,
                onOpenWidgetPromo = { promoRequested = true },
                onOpenStory = { openStory = it },
                onRequestLockScreen = { lockConsentRequested = true },
                motivationViewModel = motivationViewModel,
                onOpenMissSheet = motivationViewModel::requestMissSheet,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }

    // "What got in the way?" — from the morning notification or Insights.
    if (missSheetRequested && !habitState.isLoading) {
        MissLogSheet(
            habits = habits,
            logs = missLogs,
            onLog = motivationViewModel::logMiss,
            onDismiss = motivationViewModel::consumeMissSheet,
        )
    }

    // Full-screen story overlay (above the bar and every tab). Closing marks it seen.
    openStory?.let { story ->
        MotivationStory(
            kind = story.kind,
            habits = habits,
            userName = userName,
            savedQuotes = savedQuotes,
            onToggleQuote = settingsViewModel::setQuoteSaved,
            year = story.year ?: java.time.LocalDate.now().year,
            onClose = {
                settingsViewModel.markStorySeen(story.key)
                openStory = null
            },
        )
    }

    // Lock-screen card consent: once automatically (on a later launch than the widget
    // nudge, when nothing else is open), and whenever Settings' switch is turned on.
    val promoShowing = openStory == null && !missSheetRequested && (promoRequested || autoPromo)
    val autoLockPrompt = widgetPromoEligible && promoSeen && !lockPrompted && !lockEnabled &&
        habits.isNotEmpty() && currentTab == TopLevelDestination.Home && openStory == null && !missSheetRequested && !promoShowing
    if (lockConsentRequested || autoLockPrompt) {
        LockScreenConsentSheet(
            habits = habits,
            onAllow = { lockConsentRequested = false; settingsViewModel.enableLockScreen() },
            onDecline = { lockConsentRequested = false; settingsViewModel.declineLockScreen() },
        )
    }

    if (promoShowing) {
        WidgetPromoSheet(
            habits = habits,
            onDismiss = {
                promoRequested = false
                settingsViewModel.markWidgetPromoSeen()
            },
        )
    }
}

/**
 * Standard bottom-bar navigation transaction:
 *
 *  - **No-op when the user re-taps the active tab.** Without this guard, every
 *    tap would push a new entry and pop right back to the same screen —
 *    flicker, lost scroll position.
 *  - **popUpTo(startDestination) inclusive=false** drops everything above the
 *    graph's start so the backstack never grows by one entry per tab tap.
 *    Without it, ten taps = ten entries in history.
 *  - **saveState = true** snapshots the popped destination's state (scroll,
 *    text input, etc.) keyed by route, so we can put it back later.
 *  - **launchSingleTop = true** dedupes — if the target is already on top,
 *    reuse it instead of creating another instance. Critical for rapid taps.
 *  - **restoreState = true** restores the state we saved last time the user
 *    visited this tab.
 *
 * Together these give you the Instagram/YouTube/Twitter feel: each tab
 * preserves its own state, the back button always returns to Home (the
 * start destination) before exiting, and rapid taps don't break anything.
 */
private fun onTabSelected(
    navController: NavHostController,
    destination: TopLevelDestination,
    currentRoute: String?,
) {
    if (destination.route == currentRoute) return

    navController.navigate(destination.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
