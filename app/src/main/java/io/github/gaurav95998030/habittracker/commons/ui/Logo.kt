package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow

/** The lightning app mark: gradient rounded square. */
@Composable
fun LogoMark(size: Dp = 28.dp, modifier: Modifier = Modifier, glowing: Boolean = true) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(size * 0.3f)
    Box(
        modifier
            .size(size)
            .glow(c.primary.copy(alpha = 0.53f), 20.dp, size * 0.3f, enabled = glowing && c.isDark)
            .clip(shape)
            .background(Brush.linearGradient(listOf(c.primary, c.secondary))),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(PhIcons.Lightning, size = size * 0.58f, tint = if (c.isDark) Color(0xFF08130A) else Color.White, weight = PhWeight.Fill)
    }
}

/** Mark + "Glowstreak" wordmark. */
@Composable
fun Logo(size: Dp = 28.dp, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LogoMark(size)
        Text(
            "Glowstreak",
            style = AppTheme.textStyles.titleLarge.copy(fontSize = (size.value * 0.7f).sp, letterSpacing = (-0.5).sp),
            color = AppTheme.colors.textPrimary,
        )
    }
}
