package io.github.gaurav95998030.habittracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

/**
 * Frosted bottom navigation: icon over label, filled icon + primary tint when
 * selected. Draws behind the gesture/nav bar and pads its content above it.
 */
@Composable
fun AppBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val bar = if (c.isDark) Color(0xFF0A0E0F).copy(alpha = 0.94f) else Color.White.copy(alpha = 0.94f)
    Column(modifier.fillMaxWidth().background(bar)) {
        HorizontalDivider(thickness = 1.dp, color = c.glassBorder)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TopLevelDestination.entries.forEach { d ->
                val active = d == selected
                val tint = if (active) c.primary else c.textTertiary
                Column(
                    Modifier
                        .weight(1f)
                        .semantics { this.selected = active }
                        .clickable(interactionSource = null, indication = null, role = Role.Tab) { onSelect(d) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PhIcon(d.icon, size = 24.dp, tint = tint, weight = if (active) PhWeight.Fill else PhWeight.Regular)
                    Text(
                        d.label,
                        style = AppTheme.textStyles.caption.copy(fontSize = 10.5.sp),
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = tint,
                    )
                }
            }
        }
    }
}
