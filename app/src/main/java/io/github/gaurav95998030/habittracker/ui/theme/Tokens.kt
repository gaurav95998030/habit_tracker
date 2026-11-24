package io.github.gaurav95998030.habittracker.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil

/** One of the 8 habit accent hues. [ink] is the legible foreground on a solid [color] fill. */
@Immutable
data class Accent(val name: String, val color: Color, val ink: Color) {
    /** Storage form used on [io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit.color]. */
    val hex: String get() = HabitUtil.colorToHex(color)
}

object Accents {
    val Lime = Accent("Lime", Color(0xFFB6FF3C), Color(0xFF1C2900))
    val Emerald = Accent("Emerald", Color(0xFF34E5A0), Color(0xFF003323))
    val Teal = Accent("Teal", Color(0xFF29D4D9), Color(0xFF003134))
    val Sky = Accent("Sky", Color(0xFF3BA9FF), Color(0xFF002744))
    val Indigo = Accent("Indigo", Color(0xFF7C8CFF), Color(0xFF0C1346))
    val Magenta = Accent("Magenta", Color(0xFFFF6AD5), Color(0xFF420029))
    val Coral = Accent("Coral", Color(0xFFFF6F61), Color(0xFF420F08))
    val Amber = Accent("Amber", Color(0xFFFFB23E), Color(0xFF3A2400))

    val all: List<Accent> = listOf(Lime, Emerald, Teal, Sky, Indigo, Magenta, Coral, Amber)

    /** Parse a stored hex; falls back to [Lime] for blank/invalid values (legacy rows). */
    fun colorOf(hex: String?): Color =
        hex?.takeIf { it.isNotBlank() }?.let { runCatching { HabitUtil.hexToColor(it) }.getOrNull() }
            ?.copy(alpha = 1f) ?: Lime.color

    /** Foreground for text/icons drawn on a solid [color] fill. Works for legacy colors too. */
    fun inkFor(color: Color): Color {
        val rgb = color.copy(alpha = 1f).toArgb()
        all.firstOrNull { it.color.toArgb() == rgb }?.let { return it.ink }
        return if (color.luminance() > 0.45f) Color(0xFF06140A) else Color.White
    }

    /** Normalise any "#RRGGBB" / "#AARRGGBB" to the storage form used on habits. */
    fun storageHex(hex: String): String = HabitUtil.colorToHex(colorOf(hex))

    fun sameColor(hex: String?, accent: Accent): Boolean =
        colorOf(hex).toArgb() == accent.color.toArgb()
}

/** Corner radii (dp). */
object AppShapes {
    val card = 24.dp
    val tile = 24.dp
    val sheet = 28.dp
    val field = 16.dp
    val dot = 7.dp
    val heat = 4.dp
}

/** Motion presets from the design (spring + the CSS easing equivalents). */
object AppMotion {
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val overshoot = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    fun <T> tileTap() = spring<T>(stiffness = 420f, dampingRatio = 28f / (2 * kotlin.math.sqrt(420f)))
    fun <T> completion() = spring<T>(stiffness = 500f, dampingRatio = 16f / (2 * kotlin.math.sqrt(500f)))
    fun <T> sheetEntry() = spring<T>(stiffness = 320f, dampingRatio = 32f / (2 * kotlin.math.sqrt(320f)))
}

/**
 * Soft coloured glow behind a rounded shape — the design's dark-mode `box-shadow:
 * 0 0 Npx color`. Pass `enabled = AppTheme.colors.isDark` (glows are dark-only).
 * Uses a blur mask filter; on API < 28 software-rendered blur may be skipped,
 * which degrades gracefully to no glow.
 */
fun Modifier.glow(
    color: Color,
    radius: Dp = 16.dp,
    cornerRadius: Dp = 0.dp,
    enabled: Boolean = true,
    spread: Dp = 0.dp,
): Modifier = if (!enabled || color.alpha == 0f) this else drawBehind {
    val blurPx = radius.toPx()
    if (blurPx <= 0f) return@drawBehind
    val spreadPx = spread.toPx()
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val fp = paint.asFrameworkPaint()
        fp.isAntiAlias = true
        fp.color = color.toArgb()
        fp.maskFilter = BlurMaskFilter(blurPx / 2f, BlurMaskFilter.Blur.NORMAL)
        val cr = cornerRadius.toPx() + spreadPx
        canvas.drawRoundRect(
            left = -spreadPx, top = -spreadPx,
            right = size.width + spreadPx, bottom = size.height + spreadPx,
            radiusX = cr, radiusY = cr, paint = paint,
        )
    }
}

/**
 * Stories (daily boost, reviews, wrapped) are always dark, full-bleed, with a
 * per-slide radial tint over the background — the design's `SV` palette.
 */
object StoryPalette {
    val text = Color(0xFFF2F7F4)
    val sub = Color(0xFF9AACA8)
    val ter = Color(0xFF5E716D)
    val lime = Color(0xFFC2FF4D)
    val sky = Color(0xFF3BA9FF)
    val amber = Color(0xFFFFB23E)
    val mint = Color(0xFF34E5A0)
    val coral = Color(0xFFFF6F61)
    val base = Color(0xFF070A0B)
    val glass = Color(0xFF10171A).copy(alpha = 0.75f)
    val track = Color.White.copy(alpha = 0.22f)
    val dim = Color.White.copy(alpha = 0.07f)

    // Radial tints behind slides.
    val tintGreen = Color(0xFF24400F)
    val tintDeepGreen = Color(0xFF1D3A10)
    val tintTeal = Color(0xFF0B3A2A)
    val tintBlue = Color(0xFF0B2A3A)
    val tintAmber = Color(0xFF3A2600)
    val tintRed = Color(0xFF3A1410)
}

/** Brand mark + branded launch colours (Splash Kit). */
object BrandPalette {
    val lime = Color(0xFFC2FF4D)
    val mint = Color(0xFF34E5A0)
    val boltInk = Color(0xFF08130A)
    /** Radial glow at 50% / 35% over the launch background. */
    val launchGlowDark = Color(0xFF16301A)
    val launchGlowLight = Color(0xFFE3F2D6)
}
