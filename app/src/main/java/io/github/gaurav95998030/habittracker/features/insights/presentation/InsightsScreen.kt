package io.github.gaurav95998030.habittracker.features.insights.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.insights.domain.InsightsCalculator
import io.github.gaurav95998030.habittracker.features.insights.presentation.components.BestDayCard
import io.github.gaurav95998030.habittracker.features.insights.presentation.components.CompletionRateCard
import io.github.gaurav95998030.habittracker.features.insights.presentation.components.LongestStreaksCard
import io.github.gaurav95998030.habittracker.features.insights.presentation.components.StatPairRow
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InsightsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier,
) {
    val habitState = viewModel.uiState.collectAsState().value
    val habits = habitState.habits ?: emptyList()
    val state = remember(habits) { InsightsCalculator.build(habits) }

    var animationsArmed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationsArmed = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        if (state.isEmpty) {
            EmptyInsights()
        } else {
            InsightsContent(state = state, animationsArmed = animationsArmed)
        }
    }
}

@Composable
private fun InsightsContent(
    state: InsightsUiState,
    animationsArmed: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FadeSlide(visible = animationsArmed, delayMillis = 0) {
            Text(
                text = "Insights",
                style = AppTheme.textStyles.titleLarge.copy(
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                ),
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        FadeSlide(visible = animationsArmed, delayMillis = 80) {
            StatPairRow(
                avgWeeklyPercent = state.avgWeeklyRatePercent,
                totalCompletions = state.totalCompletions,
                visible = animationsArmed,
            )
        }

        FadeSlide(visible = animationsArmed, delayMillis = 180) {
            CompletionRateCard(
                buckets = state.weekBuckets,
                visible = animationsArmed,
            )
        }

        FadeSlide(visible = animationsArmed, delayMillis = 280) {
            BestDayCard(
                bestDay = state.bestDay,
                visible = animationsArmed,
            )
        }

        FadeSlide(visible = animationsArmed, delayMillis = 380) {
            LongestStreaksCard(
                entries = state.longestStreaks,
                visible = animationsArmed,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FadeSlide(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 450, delayMillis = delayMillis, easing = LinearOutSlowInEasing),
        ) + slideInVertically(
            initialOffsetY = { it / 6 },
            animationSpec = tween(durationMillis = 500, delayMillis = delayMillis, easing = LinearOutSlowInEasing),
        ),
    ) {
        content()
    }
}

@Composable
private fun EmptyInsights() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Insights,
            contentDescription = null,
            tint = AppTheme.colors.textTertiary,
        )
        Text(
            text = "Add a habit to start seeing your insights",
            style = AppTheme.textStyles.titleMedium.copy(color = AppTheme.colors.textPrimary),
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
        )
    }
}
