package io.github.gaurav95998030.habittracker.features.motivation.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.shareBitmap
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.DarkAppColors
import io.github.gaurav95998030.habittracker.ui.theme.LocalAppColors
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.StoryPalette
import io.github.gaurav95998030.habittracker.ui.theme.glow
import kotlinx.coroutines.launch

/** Actions a slide can trigger. */
@Stable
class StoryScope(val close: () -> Unit)

/**
 * One full-bleed slide. [tintCenter] is the radial tint's centre as a fraction of the
 * screen (the design's `radial-gradient(... at x% y%, tint, base 70%)`).
 */
class StorySlide(
    val tint: Color,
    val tintCenter: Offset,
    val content: @Composable ColumnScope.(StoryScope) -> Unit,
)

/**
 * Instagram/WhatsApp-style story viewer: segmented progress, auto-advance, tap left
 * third = back, elsewhere = forward, press-and-hold = pause, system back = close.
 * Always dark, regardless of the app theme.
 */
@Composable
fun StoryViewer(
    slides: List<StorySlide>,
    title: String,
    subtitle: String,
    color: Color,
    icon: String,
    onClose: () -> Unit,
    onFinished: () -> Unit = onClose,
    durationMillis: Int = 6500,
) {
    var index by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    val progress = remember(index) { Animatable(0f) }
    val last = slides.lastIndex
    val next by rememberUpdatedState { if (index < last) index++ else onFinished() }
    val prev by rememberUpdatedState { if (index > 0) index-- }
    val scope = StoryScope(close = onClose)

    BackHandler(onBack = onClose)
    LightStatusBarIcons()

    LaunchedEffect(index, paused) {
        if (paused) return@LaunchedEffect
        val remaining = ((1f - progress.value) * durationMillis).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(remaining, easing = LinearEasing))
        next()
    }

    val slide = slides[index]
    val tint by animateColorAsState(slide.tint, tween(450), label = "storyTint")

    androidx.compose.runtime.CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        Box(
            Modifier
                .fillMaxSize()
                .background(StoryPalette.base)
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            0f to tint, 0.7f to StoryPalette.base,
                            center = Offset(slide.tintCenter.x * size.width, slide.tintCenter.y * size.height),
                            radius = size.maxDimension * 0.85f,
                        )
                    )
                },
        ) {
            // Tap zones live *behind* the content, so slide buttons (Share, Close, …)
            // receive their own taps and never also advance the story.
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                paused = true
                                tryAwaitRelease()
                                paused = false
                            },
                            onLongPress = { /* hold = pause only */ },
                            onTap = { if (it.x < size.width / 3f) prev() else next() },
                        )
                    },
            )
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                // Segmented progress.
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    slides.indices.forEach { j ->
                        val f = when {
                            j < index -> 1f
                            j == index -> progress.value
                            else -> 0f
                        }
                        Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(StoryPalette.track)) {
                            Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(CircleShape).background(StoryPalette.text))
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatusRing(segments = 1, color = color, seen = false, size = 36.dp, inner = StoryPalette.base.copy(alpha = 0.85f)) {
                        PhIcon(icon, size = 14.dp, tint = color, weight = PhWeight.Fill)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(title, style = AppTheme.textStyles.titleSmall.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = StoryPalette.text)
                        Text(subtitle, style = AppTheme.textStyles.caption.copy(fontSize = 11.5.sp), color = StoryPalette.sub)
                    }
                    if (paused) PhIcon(PhIcons.Pause, size = 16.dp, tint = StoryPalette.sub, weight = PhWeight.Fill)
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable(onClickLabel = "Close", onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) { PhIcon(PhIcons.X, size = 18.dp, tint = StoryPalette.text, weight = PhWeight.Bold) }
                }
                AnimatedContent(
                    targetState = index,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                    label = "storySlide",
                    modifier = Modifier.weight(1f),
                ) { i ->
                    Column(Modifier.fillMaxSize().padding(start = 26.dp, end = 26.dp, top = 34.dp, bottom = 30.dp)) {
                        slides[i].content(this, scope)
                    }
                }
            }
        }
    }
}

/** Stories are dark — force light status/nav icons while one is open. */
@Composable
private fun LightStatusBarIcons() {
    val view = LocalView.current
    val activity = LocalActivity.current ?: return
    DisposableEffect(view) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        val prevStatus = controller.isAppearanceLightStatusBars
        val prevNav = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = prevStatus
            controller.isAppearanceLightNavigationBars = prevNav
        }
    }
}

/**
 * Segmented story ring — one arc per slide, grey once seen. Unseen rings glow in dark
 * mode. [inner] is the disc between the ring and the content.
 */
@Composable
fun StatusRing(
    segments: Int,
    color: Color,
    seen: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    inner: Color = AppTheme.colors.background,
    content: @Composable () -> Unit,
) {
    val c = AppTheme.colors
    val ring = if (seen) (if (c.isDark) Color.White.copy(alpha = 0.22f) else Color(0xFF0C1110).copy(alpha = 0.2f)) else color
    Box(
        modifier
            .size(size)
            .glow(color.copy(alpha = 0.33f), 16.dp, size / 2, enabled = !seen && c.isDark),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val gap = if (segments > 1) 9f else 0f
            val per = 360f / segments
            val inset = stroke / 2
            repeat(segments) { k ->
                drawArc(
                    color = ring,
                    startAngle = -90f + k * per + gap / 2,
                    sweepAngle = per - gap,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke),
                    style = Stroke(stroke),
                )
            }
            drawCircle(inner, radius = this.size.minDimension / 2 - stroke)
        }
        Box(Modifier.padding(6.dp).fillMaxSize().clip(CircleShape), contentAlignment = Alignment.Center) { content() }
    }
}

// ── Slide typography (the design's Kick / Big / Line / Note) ──

@Composable
fun StoryKicker(text: String, color: Color) = Text(
    text.uppercase(),
    style = AppTheme.textStyles.labelLarge.copy(fontSize = 13.sp, letterSpacing = 1.sp),
    fontWeight = FontWeight.SemiBold,
    color = color,
)

@Composable
fun StoryBig(text: String, size: TextUnit = 120.sp, modifier: Modifier = Modifier) = Text(
    text,
    style = AppTheme.textStyles.displayLarge.copy(
        fontSize = size,
        lineHeight = size * 0.98f,
        letterSpacing = if (size.value > 80) (-4).sp else (-2).sp,
    ),
    color = StoryPalette.text,
    modifier = modifier,
)

@Composable
fun StoryLine(text: String, modifier: Modifier = Modifier, color: Color = StoryPalette.text, size: TextUnit = 24.sp) = Text(
    text,
    style = AppTheme.textStyles.titleMedium.copy(fontSize = size, lineHeight = size * 1.2f, letterSpacing = (-0.4).sp),
    color = color,
    modifier = modifier.padding(top = 12.dp),
)

@Composable
fun StoryNote(text: String, modifier: Modifier = Modifier) = Text(
    text,
    style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp, lineHeight = 23.sp),
    color = StoryPalette.sub,
    modifier = modifier.padding(top = 14.dp),
)

/** "+12% vs last week" pill. */
@Composable
fun DeltaChip(delta: Int, label: String) {
    val col = if (delta >= 0) StoryPalette.mint else StoryPalette.coral
    Row(
        Modifier
            .clip(CircleShape)
            .background(col.copy(alpha = 0.13f))
            .border(1.dp, col.copy(alpha = 0.33f), CircleShape)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PhIcon(if (delta >= 0) PhIcons.TrendUp else PhIcons.TrendDown, size = 15.dp, tint = col, weight = PhWeight.Bold)
        Text("${if (delta >= 0) "+" else ""}$delta% $label", style = AppTheme.textStyles.labelLarge.copy(fontSize = 13.sp), fontWeight = FontWeight.SemiBold, color = col)
    }
}

/**
 * The shareable "wrapped" card. Everything inside is recorded into a graphics layer so
 * [ShareCardButton] can turn it into a PNG.
 */
@Composable
fun ShareCard(
    layer: GraphicsLayer,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier
            .fillMaxWidth()
            .glow(accent.copy(alpha = 0.1f), 40.dp, 26.dp)
            .drawWithContent {
                layer.record { this@drawWithContent.drawContent() }
                drawLayer(layer)
            }
            .clip(shape)
            // Opaque so the exported PNG looks the same outside the story.
            .background(Brush.linearGradient(listOf(Color(0xFF121B1E), Color(0xFF0B1113))))
            .border(1.dp, accent.copy(alpha = 0.22f), shape)
            .padding(20.dp),
        content = content,
    )
}

@Composable
fun rememberShareLayer(): GraphicsLayer = rememberGraphicsLayer()

/** Lime primary "Share …" button that exports [layer] and opens the share sheet. */
@Composable
fun ShareCardButton(label: String, layer: GraphicsLayer, fileName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    HfButton(
        label,
        onClick = {
            scope.launch {
                val bmp = layer.toImageBitmap().asAndroidBitmap()
                shareBitmap(context, bmp, fileName, label)
            }
        },
        icon = PhIcons.ShareNetwork,
        size = HfButtonSize.Large,
        fullWidth = true,
    )
}

/** Header used inside share cards: logo mark + title. */
@Composable
fun ShareCardHead(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(listOf(StoryPalette.lime, StoryPalette.mint))),
            contentAlignment = Alignment.Center,
        ) { PhIcon(PhIcons.Lightning, size = 15.dp, tint = Color(0xFF08130A), weight = PhWeight.Fill) }
        Text(title, style = AppTheme.textStyles.titleSmall, fontWeight = FontWeight.SemiBold, color = StoryPalette.text)
    }
}

/** 2×2 stat grid inside share cards. */
@Composable
fun ShareStatGrid(items: List<Pair<String, String>>) {
    Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { (v, l) ->
                    Column(Modifier.weight(1f).semantics { contentDescription = "$v $l" }) {
                        Text(v, style = AppTheme.textStyles.titleLarge.copy(fontSize = 32.sp, lineHeight = 32.sp, letterSpacing = (-1).sp), color = StoryPalette.text)
                        Text(l, style = AppTheme.textStyles.bodySmall, color = StoryPalette.sub, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
