package io.github.gaurav95998030.habittracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.navigation.AppBottomBar
import io.github.gaurav95998030.habittracker.navigation.AppNavHost
import io.github.gaurav95998030.habittracker.navigation.TopLevelDestination

/**
 * Root screen of the app.
 *
 * Owns the NavController, observes the current backstack entry, derives the
 * "selected tab" from it, and handles the navigation transaction when a tab
 * is tapped.
 *
 * The four flags on navigate() are load-bearing — they are the standard
 * recipe for a bottom-bar-style NavHost. See onTabSelected() below.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(habitViewModel: HabitViewModel) {
    val navController = rememberNavController()

    // currentBackStackEntryAsState recomposes when the active destination
    // changes — so the bar's selected state stays in sync with whichever
    // route the NavController is on (e.g. system back button).
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selected = TopLevelDestination.values()
        .firstOrNull { it.route == currentRoute }
        ?: TopLevelDestination.Start

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selected = selected,
                onSelect = { destination ->
                    onTabSelected(navController, destination, currentRoute)
                },
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            habitViewModel = habitViewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
    navController: androidx.navigation.NavHostController,
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
