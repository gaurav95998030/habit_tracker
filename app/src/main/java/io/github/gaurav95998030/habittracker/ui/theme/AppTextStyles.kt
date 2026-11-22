package io.github.gaurav95998030.habittracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale — 1:1 with the design's TYPE tokens. Display styles use Clash Display,
 * text/label styles use Space Grotesk. Colors are applied at the call site from
 * [AppTheme.colors]. One-off sizes in the design are expressed as
 * `AppTheme.textStyles.<base>.copy(fontSize = …)` so the family/weight still flow
 * from here.
 *
 * [labelLarge], [labelMedium] and [caption] predate the redesign and are kept for
 * existing call sites.
 */
@Immutable
data class AppTextStyles(
    val displayLarge: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    /** Uppercase section label (apply `.uppercase()` to the text). */
    val labelSmall: TextStyle,
    val caption: TextStyle,
)

internal val DefaultAppTextStyles = AppTextStyles(
    displayLarge = TextStyle(
        fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ClashDisplay, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = ClashDisplay, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp,
    ),
    caption = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.2.sp,
    ),
)

val LocalAppTextStyles = staticCompositionLocalOf { DefaultAppTextStyles }
