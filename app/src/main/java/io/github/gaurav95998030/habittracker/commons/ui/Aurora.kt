package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.PhosphorBold
import io.github.gaurav95998030.habittracker.ui.theme.PhosphorFill
import io.github.gaurav95998030.habittracker.ui.theme.PhosphorRegular
import io.github.gaurav95998030.habittracker.ui.theme.glow

// Aurora UI primitives — Compose ports of the design's ui.jsx.

enum class PhWeight { Regular, Bold, Fill }

/**
 * Phosphor icon rendered from the bundled icon font. [glyph] is a [PhIcons] constant.
 * Sized in dp (independent of the user's font scale), like a vector icon.
 */
@Composable
fun PhIcon(
    glyph: String,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = AppTheme.colors.textSecondary,
    weight: PhWeight = PhWeight.Regular,
) {
    val fontSize = with(LocalDensity.current) { size.toSp() }
    val family = when (weight) {
        PhWeight.Regular -> PhosphorRegular
        PhWeight.Bold -> PhosphorBold
        PhWeight.Fill -> PhosphorFill
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Text(
            text = glyph,
            color = tint,
            style = TextStyle(
                fontFamily = family,
                fontSize = fontSize,
                lineHeight = fontSize,
                textAlign = TextAlign.Center,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}

/** Icon by stored "ph-xxx" name, falling back to a sparkle for unknown names. */
@Composable
fun PhIconNamed(
    name: String?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = AppTheme.colors.textSecondary,
    weight: PhWeight = PhWeight.Bold,
) = PhIcon(PhIcons.byName(name) ?: PhIcons.Sparkle, modifier, size, tint, weight)

enum class HfButtonVariant { Primary, Secondary, Ghost, Danger }
enum class HfButtonSize { Small, Medium, Large }

/** Pill button with press-scale. */
@Composable
fun HfButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HfButtonVariant = HfButtonVariant.Primary,
    size: HfButtonSize = HfButtonSize.Medium,
    icon: String? = null,
    iconWeight: PhWeight = PhWeight.Bold,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
) {
    val c = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.96f else 1f, AppMotion.tileTap(), label = "btnScale")
    val (padH, padV, fs) = when (size) {
        HfButtonSize.Small -> Triple(16.dp, 9.dp, 13f)
        HfButtonSize.Medium -> Triple(22.dp, 13.dp, 15f)
        HfButtonSize.Large -> Triple(26.dp, 16.dp, 16f)
    }
    val shape = CircleShape
    val (bg, fg) = when (variant) {
        HfButtonVariant.Primary -> c.primary to c.onPrimary
        HfButtonVariant.Secondary -> c.glassFill to c.textPrimary
        HfButtonVariant.Ghost -> Color.Transparent to c.textSecondary
        HfButtonVariant.Danger -> Color.Transparent to c.error
    }
    val border = when (variant) {
        HfButtonVariant.Secondary -> c.glassBorder
        HfButtonVariant.Danger -> c.error.copy(alpha = 0.33f)
        else -> null
    }
    Row(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.4f)
            .glow(c.primary.copy(alpha = 0.33f), 24.dp, 999.dp, enabled = c.isDark && enabled && variant == HfButtonVariant.Primary)
            .clip(shape)
            .background(bg)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = padH, vertical = padV),
        horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) PhIcon(icon, size = (fs + 3).dp, tint = fg, weight = iconWeight)
        Text(
            text = text,
            style = AppTheme.textStyles.titleSmall.copy(fontSize = fs.sp, letterSpacing = (-0.1).sp),
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = fg,
            maxLines = 1,
        )
    }
}

/** Round glass icon button (close, back, header actions). */
@Composable
fun HfIconButton(
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    iconSize: Dp = 16.dp,
    tint: Color = AppTheme.colors.textSecondary,
    shape: RoundedCornerShape = CircleShape,
    enabled: Boolean = true,
    contentDescription: String? = null,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(shape)
            .background(c.glassFill)
            .border(1.dp, c.glassBorder, shape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(glyph, size = iconSize, tint = tint, weight = PhWeight.Bold)
        badge?.invoke(this)
    }
}

/** Selectable chip (category / filter). */
@Composable
fun HfChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    accent: Color = AppTheme.colors.primary,
) {
    val c = AppTheme.colors
    val bg by animateColorAsState(
        if (active) accent.copy(alpha = if (c.isDark) 0.14f else 0.11f) else c.glassFill, label = "chipBg",
    )
    val borderColor = if (active) accent.copy(alpha = if (c.isDark) 0.53f else 0.4f) else c.glassBorder
    val fg = if (active) accent else c.textSecondary
    Row(
        modifier = modifier
            .glow(accent.copy(alpha = 0.2f), 16.dp, 999.dp, enabled = active && c.isDark)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, borderColor, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            PhIconNamed(icon, size = 15.dp, tint = if (active) accent else c.textTertiary,
                weight = if (active) PhWeight.Fill else PhWeight.Regular)
        }
        Text(label, style = AppTheme.textStyles.labelLarge.copy(fontSize = 13.sp), color = fg, maxLines = 1)
    }
}

/** Uppercase section label used above form groups and lists. */
@Composable
fun HfSectionLabel(text: String, modifier: Modifier = Modifier, bottom: Dp = 12.dp) {
    Text(
        text = text.uppercase(),
        style = AppTheme.textStyles.labelSmall.copy(fontSize = 12.sp, letterSpacing = 0.5.sp),
        color = AppTheme.colors.textTertiary,
        modifier = modifier.padding(bottom = bottom),
    )
}

/** Text field with leading icon, focus ring, optional label and multiline mode. */
@Composable
fun HfField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    icon: String? = null,
    multiline: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    val c = AppTheme.colors
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(if (focused) c.primary else c.glassBorder, label = "fieldBorder")
    val shape = RoundedCornerShape(AppShapes.field)
    val textStyle = AppTheme.textStyles.bodyLarge.copy(color = c.textPrimary, lineHeight = 22.sp)
    androidx.compose.foundation.layout.Column(modifier) {
        if (label != null) HfSectionLabel(label, bottom = 8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focused && c.isDark) Modifier.border(4.dp, c.primary.copy(alpha = 0.13f), RoundedCornerShape(AppShapes.field + 3.dp)).padding(3.dp) else Modifier.padding(3.dp))
                .clip(shape)
                .background(c.fieldFill)
                .border(1.5.dp, borderColor, shape)
                .then(if (multiline) Modifier.heightIn(min = 52.dp).padding(16.dp, 14.dp) else Modifier.height(52.dp).padding(horizontal = 16.dp)),
            verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                PhIcon(icon, size = 19.dp, tint = if (focused) c.primary else c.textTertiary,
                    modifier = if (multiline) Modifier.offset(y = 2.dp) else Modifier)
            }
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle, color = c.textTertiary, maxLines = if (multiline) 2 else 1)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = textStyle,
                    singleLine = !multiline,
                    minLines = if (multiline) 2 else 1,
                    maxLines = if (multiline) 5 else 1,
                    cursorBrush = SolidColor(c.primary),
                    keyboardOptions = KeyboardOptions(capitalization = capitalization),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
                )
            }
        }
    }
}

/** Pill toggle with springy thumb. */
@Composable
fun HfToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = AppTheme.colors.primary,
) {
    val c = AppTheme.colors
    val track by animateColorAsState(
        if (checked) accent else if (c.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF0C1110).copy(alpha = 0.18f),
        tween(180), label = "toggleTrack",
    )
    val thumbX by animateDpAsState(if (checked) 25.dp else 3.dp, AppMotion.completion(), label = "toggleThumb")
    Box(
        modifier = modifier
            .size(52.dp, 30.dp)
            .glow(accent.copy(alpha = 0.33f), 16.dp, 999.dp, enabled = checked && c.isDark)
            .clip(CircleShape)
            .background(track)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = thumbX, y = 3.dp)
                .size(24.dp)
                .glow(Color.Black.copy(alpha = 0.35f), 6.dp, 12.dp)
                .clip(CircleShape)
                .background(if (checked) c.onPrimary else Color.White),
        )
    }
}

/** 8-swatch accent picker. [selectedHex] is a stored habit color. */
@Composable
fun HfSwatches(selectedHex: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Accents.all.forEach { a ->
            val sel = Accents.sameColor(selectedHex, a)
            val scale by animateFloatAsState(if (sel) 1.12f else 1f, AppMotion.completion(), label = "swatch")
            Box(
                Modifier
                    .padding(5.dp)
                    .scale(scale)
                    .size(38.dp)
                    .glow(a.color.copy(alpha = if (sel) 0.6f else 0.33f), if (sel) 20.dp else 8.dp, 19.dp, spread = if (sel) 5.dp else 0.dp)
                    .then(if (sel) Modifier.border(2.dp, a.color, CircleShape).padding(4.dp).border(1.dp, c.background, CircleShape) else Modifier)
                    .clip(CircleShape)
                    .background(a.color)
                    .clickable(role = Role.RadioButton, onClickLabel = a.name) { onSelect(a.hex) },
                contentAlignment = Alignment.Center,
            ) {
                if (sel) PhIcon(PhIcons.Check, size = 18.dp, tint = a.ink, weight = PhWeight.Bold)
            }
        }
    }
}

data class SegOption<T>(val value: T, val label: String, val icon: String? = null)

/** Segmented radio (theme picker). */
@Composable
fun <T> HfSegRadio(options: List<SegOption<T>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(c.glassFill)
            .border(1.dp, c.glassBorder, CircleShape)
            .padding(3.dp),
    ) {
        options.forEach { o ->
            val sel = o.value == selected
            val bg by animateColorAsState(if (sel) c.primary else Color.Transparent, tween(180), label = "seg")
            val fg = if (sel) c.onPrimary else c.textSecondary
            Row(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(bg)
                    .clickable(role = Role.RadioButton) { onSelect(o.value) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (o.icon != null) PhIcon(o.icon, size = 15.dp, tint = fg, weight = PhWeight.Bold)
                Text(o.label, style = AppTheme.textStyles.labelLarge.copy(fontSize = 13.sp), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = fg)
            }
        }
    }
}

/** Shimmer placeholder block. */
@Composable
fun HfShimmer(modifier: Modifier = Modifier, radius: Dp = 8.dp) {
    val c = AppTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 2f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing)),
        label = "shimmerX",
    )
    Box(
        modifier
            .clip(RoundedCornerShape(radius))
            .drawBehind {
                val w = size.width
                drawRect(
                    Brush.linearGradient(
                        0.3f to c.surface, 0.5f to c.surface2, 0.7f to c.surface,
                        start = Offset(progress * w - w, 0f),
                        end = Offset(progress * w + w, 0f),
                    )
                )
            },
    )
}

/** Sheet drag grip. */
@Composable
fun HfSheetGrip(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(40.dp, 5.dp)
                .alpha(0.5f)
                .clip(CircleShape)
                .background(AppTheme.colors.textTertiary),
        )
    }
}

/** Card container: surface + hairline glass border. */
@Composable
fun HfCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    radius: Dp = AppShapes.card,
    background: Brush? = null,
    borderColor: Color = AppTheme.colors.glassBorder,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    androidx.compose.foundation.layout.Column(
        modifier
            .clip(shape)
            .then(if (background != null) Modifier.background(background) else Modifier.background(AppTheme.colors.surface))
            .border(1.dp, borderColor, shape)
            .padding(padding),
        content = content,
    )
}

/** Dark-mode accent wash used on hero cards: `linear-gradient(135deg, accent@a, surface)`. */
@Composable
fun accentWash(accent: Color, alpha: Float = 0.1f): Brush {
    val c = AppTheme.colors
    return if (c.isDark) Brush.linearGradient(listOf(accent.copy(alpha = alpha), c.surface))
    else SolidColor(c.surface)
}

/** Small spacer helper. */
@Composable
fun HSpace(w: Dp) = Spacer(Modifier.width(w))

/** Small spacer helper. */
@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))

/** A "clickable without ripple" convenience used across the redesign. */
fun Modifier.noRippleClick(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = null,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
