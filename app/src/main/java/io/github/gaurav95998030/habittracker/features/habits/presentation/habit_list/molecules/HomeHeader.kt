package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HeatmapViewMode
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeHeader(
    userName: String,
    viewMode: HeatmapViewMode,
    onToggleViewMode: () -> Unit,
    onOpenNotifications: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val greeting = greetingForNow()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, $userName",
                style = AppTheme.textStyles.bodyMedium.copy(
                    color = colors.textSecondary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Today",
                color = colors.textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = AppTheme.textStyles.titleLarge.copy(
                    color = colors.textPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }

        ViewModeToggle(
            viewMode = viewMode,
            onClick = onToggleViewMode,
        )

        Spacer(modifier = Modifier.width(10.dp))

        IconBubble(
            icon = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            onClick = onOpenNotifications,
        )

        Spacer(modifier = Modifier.width(10.dp))

        AvatarBubble(
            initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        )
    }
}

@Composable
private fun IconBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.divider, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ViewModeToggle(
    viewMode: HeatmapViewMode,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val isWeek = viewMode == HeatmapViewMode.Week

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isWeek) colors.primary.copy(alpha = 0.16f) else colors.surface
            )
            .border(
                width = 1.dp,
                color = if (isWeek) colors.primary.copy(alpha = 0.6f) else colors.divider,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isWeek) Icons.Default.CalendarMonth else Icons.Default.CalendarViewWeek,
            contentDescription = if (isWeek) "Switch to year heatmap" else "Switch to week heatmap",
            tint = if (isWeek) colors.primary else colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AvatarBubble(initial: String) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.divider, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = colors.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun greetingForNow(): String {
    val hour = runCatching { LocalTime.now().hour }.getOrDefault(9)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
}
