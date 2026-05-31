package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Drop-in pull-to-refresh wrapper. Manages its own `isRefreshing` flag so callers
 * only need to provide a suspend `onRefresh`.
 *
 * - Adds haptic feedback on trigger and completion.
 * - Enforces a minimum spinner duration so fast refreshes (e.g. local Room reads)
 *   don't collapse the indicator in the same frame they show it.
 *
 * The wrapper itself does not scroll — place a scrollable (LazyColumn etc.) inside
 * [content] so the gesture has something to attach to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefresh(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minSpinMillis: Long = 700L,
    indicatorAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val containerColor: Color = AppTheme.colors.surface
    val indicatorColor: Color = AppTheme.colors.primary

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            if (!enabled || isRefreshing) return@PullToRefreshBox
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                isRefreshing = true
                val startedAt = System.currentTimeMillis()
                try {
                    onRefresh()
                } finally {
                    val elapsed = System.currentTimeMillis() - startedAt
                    val remaining = minSpinMillis - elapsed
                    if (remaining > 0) delay(remaining)
                    isRefreshing = false
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        },
        modifier = modifier,
        state = state,
        contentAlignment = Alignment.TopStart,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(indicatorAlignment),
                containerColor = containerColor,
                color = indicatorColor,
            )
        },
        content = content,
    )
}
