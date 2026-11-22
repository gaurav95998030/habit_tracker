package io.github.gaurav95998030.habittracker.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.gaurav95998030.habittracker.R

/** Display face — titles, numbers, buttons. (Clash Display, Fontshare ITF FFL.) */
val ClashDisplay = FontFamily(
    Font(R.font.clash_display_regular, FontWeight.Normal),
    Font(R.font.clash_display_medium, FontWeight.Medium),
    Font(R.font.clash_display_semibold, FontWeight.SemiBold),
    Font(R.font.clash_display_bold, FontWeight.Bold),
)

/** Text face — body copy and labels. (Space Grotesk, OFL.) */
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

/** Phosphor icon fonts, one family per weight. See [PhIcons] / [PhIcon]. */
val PhosphorRegular = FontFamily(Font(R.font.phosphor_regular))
val PhosphorBold = FontFamily(Font(R.font.phosphor_bold))
val PhosphorFill = FontFamily(Font(R.font.phosphor_fill))
