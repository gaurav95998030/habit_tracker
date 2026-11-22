package io.github.gaurav95998030.habittracker.features.habits.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfShimmer
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

private val RevealWidth = 84.dp
private val MaxDrag = 96.dp
private val SnapThreshold = 52.dp

/**
 * Home list tile — idle / completed-today / paused, with swipe-left to reveal Delete.
 *
 * Paused tiles dim and swap the check circle for a play button that resumes the habit.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitTile(
    habit: Habit,
    onToggleToday: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val today = LocalDate.now()
    val completed = remember(habit.completedDates) { HabitStats.completedSet(habit) }
    val doneToday = today in completed
    val streak = remember(completed, today) { HabitUtil.currentStreak(completed, today) }
    val inactive = !habit.isActive
    val baseColor = Accents.colorOf(habit.color)
    // Paused habits read desaturated (the design's grayscale(0.6)).
    val color = if (inactive) lerp(baseColor, Color(0xFF8A9592), 0.6f) else baseColor
    val icon = HabitCatalog.iconFor(habit)

    val density = LocalDensity.current
    val revealPx = with(density) { RevealWidth.toPx() }
    val maxPx = with(density) { MaxDrag.toPx() }
    val snapPx = with(density) { SnapThreshold.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(AppShapes.tile)

    Box(
        modifier
            .fillMaxWidth()
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Delete ${habit.name}") { onDelete(); true },
                )
            },
    ) {
        // Delete action behind — only mounted while swiping so it never bleeds through.
        if (offsetX.value < -2f) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(c.error)
                    .clickable { onDelete() }
                    .padding(end = 26.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PhIcon(PhIcons.Trash, size = 22.dp, tint = c.onError, weight = PhWeight.Fill)
                    Text("Delete", style = AppTheme.textStyles.caption.copy(fontWeight = FontWeight.SemiBold), color = c.onError)
                }
            }
        }

        Column(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offsetX.snapTo((offsetX.value + delta).coerceIn(-maxPx, 0f)) }
                    },
                    onDragStopped = {
                        offsetX.animateTo(if (offsetX.value < -snapPx) -revealPx else 0f, AppMotion.sheetEntry())
                    },
                )
                .glow(
                    if (doneToday) color.copy(alpha = 0.13f) else Color.Black.copy(alpha = 0.28f),
                    if (doneToday) 26.dp else 18.dp, AppShapes.tile,
                    enabled = c.isDark,
                )
                .alpha(if (inactive) 0.55f else 1f)
                .clip(shape)
                .background(
                    if (c.isDark && doneToday) Brush.linearGradient(listOf(color.copy(alpha = 0.11f), c.surface))
                    else Brush.linearGradient(listOf(c.surface, c.surface)),
                )
                .border(1.dp, if (doneToday && c.isDark) color.copy(alpha = 0.3f) else c.glassBorder, shape)
                .clickable {
                    if (offsetX.value < -4f) scope.launch { offsetX.animateTo(0f, AppMotion.sheetEntry()) }
                    else onOpen()
                }
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HabitIconBadge(icon = icon, color = color)
                HSpace(13.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        style = AppTheme.textStyles.titleMedium.copy(fontSize = 17.sp, letterSpacing = (-0.2).sp),
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                        Text(habit.category, style = AppTheme.textStyles.bodySmall, color = c.textSecondary, maxLines = 1)
                        if (inactive) {
                            Text(
                                "Paused",
                                style = AppTheme.textStyles.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = c.textTertiary,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .clip(CircleShape)
                                    .background(c.glassFill)
                                    .border(1.dp, c.glassBorder, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                if (inactive) {
                    Box(
                        Modifier.size(46.dp).clip(CircleShape).clickable(onClickLabel = "Resume ${habit.name}") { onResume() },
                        contentAlignment = Alignment.Center,
                    ) {
                        PhIcon(PhIcons.PlayCircle, size = 34.dp, tint = c.textTertiary, weight = PhWeight.Fill)
                    }
                } else {
                    CheckCircle(done = doneToday, color = color, onToggle = onToggleToday, habitName = habit.name)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                WeeklyGrid(completed = completed, color = color, today = today)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PhIcon(PhIcons.Fire, size = 16.dp, tint = if (streak > 0) color else c.textTertiary, weight = PhWeight.Fill)
                    Text(
                        streak.toString(),
                        style = AppTheme.textStyles.titleMedium.copy(fontSize = 17.sp, letterSpacing = (-0.3).sp),
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                    )
                }
            }
        }
    }
}

/** Loading placeholder shaped like a tile. */
@Composable
fun HabitTileShimmer(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(AppShapes.tile)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.glassBorder, shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HfShimmer(Modifier.size(46.dp), radius = 14.dp)
            HSpace(13.dp)
            Column(Modifier.weight(1f)) {
                HfShimmer(Modifier.fillMaxWidth(0.55f).height(15.dp), radius = 6.dp)
                HfShimmer(Modifier.padding(top = 8.dp).fillMaxWidth(0.32f).height(11.dp), radius = 6.dp)
            }
            HfShimmer(Modifier.size(46.dp), radius = 23.dp)
        }
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            HfShimmer(Modifier.size(190.dp, 22.dp), radius = 7.dp)
            HfShimmer(Modifier.size(40.dp, 18.dp), radius = 6.dp)
        }
    }
}
