package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.ReviewStats
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.StoryPalette
import java.time.LocalDate
import java.time.YearMonth

/** One ring in the Home status row. [key] identifies "seen" (per day / week / month / year). */
data class StoryItem(
    val kind: StoryKind,
    val key: String,
    val label: String,
    val segments: Int,
    val color: Color,
    val icon: String,
    val year: Int? = null,
)

/**
 * Which stories to offer today: the daily boost and your week always (once there's an
 * active habit), last month's review when it had any check-ins, and "Wrapped" in
 * December/January.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun storyItems(habits: List<Habit>, today: LocalDate = LocalDate.now()): List<StoryItem> {
    if (!hasRecapData(habits)) return emptyList()
    val out = mutableListOf(
        StoryItem(StoryKind.Boost, ReviewStats.boostKey(today), "Daily boost", 3, StoryPalette.lime, PhIcons.Sparkle),
        StoryItem(StoryKind.Week, ReviewStats.weekKey(today), "Your week", 4, StoryPalette.sky, PhIcons.CalendarCheck),
    )
    val lastMonth = YearMonth.from(today).minusMonths(1).toString()
    if (habits.any { h -> h.isActive && h.completedDates.any { it.startsWith(lastMonth) } }) {
        out += StoryItem(StoryKind.Month, ReviewStats.monthKey(today), lastMonthName(today), 4, StoryPalette.amber, PhIcons.CalendarDots)
    }
    ReviewStats.wrappedYear(today)?.let { y ->
        if (habits.any { h -> h.completedDates.any { it.startsWith("$y-") } }) {
            out += StoryItem(StoryKind.Year, ReviewStats.yearKey(y), "$y Wrapped", 4, StoryPalette.lime, PhIcons.Trophy, year = y)
        }
    }
    return out
}

/** Horizontal row of story rings at the top of Today. Unseen rings glow; seen ones go grey. */
@Composable
fun StoryStrip(
    items: List<StoryItem>,
    seen: Set<String>,
    onOpen: (StoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    LazyRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(items, key = { it.key }) { item ->
            val wasSeen = item.key in seen
            Column(
                Modifier
                    .width(70.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClickLabel = "Open ${item.label}") { onOpen(item) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                StatusRing(segments = item.segments, color = item.color, seen = wasSeen) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (c.isDark) Brush.radialGradient(listOf(item.color.copy(alpha = 0.27f), c.surface))
                                else Brush.linearGradient(listOf(item.color.copy(alpha = 0.13f), item.color.copy(alpha = 0.13f)))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        PhIcon(item.icon, size = 22.dp, tint = if (c.isDark) item.color else c.textPrimary, weight = PhWeight.Fill)
                    }
                }
                Text(
                    item.label,
                    style = AppTheme.textStyles.caption.copy(fontSize = 11.5.sp),
                    fontWeight = if (wasSeen) FontWeight.Medium else FontWeight.SemiBold,
                    color = if (wasSeen) c.textTertiary else c.textPrimary,
                    maxLines = 1,
                )
            }
        }
    }
}
