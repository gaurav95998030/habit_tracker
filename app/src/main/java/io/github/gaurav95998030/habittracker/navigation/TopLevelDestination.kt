package io.github.gaurav95998030.habittracker.navigation

import io.github.gaurav95998030.habittracker.ui.theme.PhIcons

/**
 * The three top-level tabs reachable from the bottom navigation bar.
 *
 * Each destination owns its route (used by NavController), a label, and a Phosphor
 * glyph (rendered regular when unselected, fill when selected). Keeping all of this
 * in one place is the canonical way to avoid drift between the bar and the NavHost.
 */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: String,
) {
    Home(route = "home", label = "Today", icon = PhIcons.House),
    Insights(route = "insights", label = "Insights", icon = PhIcons.ChartLineUp),
    Settings(route = "settings", label = "Settings", icon = PhIcons.Gear);

    companion object {
        val Start: TopLevelDestination = Home
    }
}
