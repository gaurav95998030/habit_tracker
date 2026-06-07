package io.github.gaurav95998030.habittracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

private val BarHeight = 64.dp

/**
 * Flat, minimal bottom navigation bar.
 *
 * - Vertical stack per item: icon on top, label below.
 * - Always-visible labels (no animated expand/collapse).
 * - Selected = filled icon + primary color + slightly bolder label.
 * - Unselected = outlined icon + tertiary text color.
 * - No pill, no sliding indicator — color alone signals the active tab.
 *
 * Uses MaterialTheme.colorScheme.primary so the accent matches the dynamic
 * color scheme (the same accent driving the FAB), instead of the static
 * AppTheme.colors.primary token.
 */
@Composable
fun AppBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val destinations = remember { TopLevelDestination.values().toList() }
    val colors = AppTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colors.divider.copy(alpha = 0.4f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(BarHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                destinations.forEach { destination ->
                    BottomBarItem(
                        destination = destination,
                        isSelected = destination == selected,
                        onClick = { onSelect(destination) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = AppTheme.colors.textTertiary
    val contentColor = if (isSelected) selectedColor else unselectedColor

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = destination.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = destination.label,
            style = AppTheme.textStyles.labelSmall,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

