package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.BrandPalette
import io.github.gaurav95998030.habittracker.ui.theme.glow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated branded launch (Splash Kit, ~1.2 s) shown right after the system splash on
 * cold starts: icon springs 0.6 → 1, ring draws to 87%, wordmark + tagline rise in,
 * loader fills, then [onFinished]. Follows the in-app theme (AppTheme), so light/dark
 * match the screen it cross-fades into.
 */
@Composable
fun BrandedLaunch(onFinished: () -> Unit) {
    val c = AppTheme.colors
    val finish by rememberUpdatedState(onFinished)
    val ring = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0.6f) }
    val textAlpha = remember { Animatable(0f) }
    val loader = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { iconScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f)) }
        launch { ring.animateTo(0.87f, tween(700, easing = FastOutSlowInEasing)) }
        launch { delay(250); textAlpha.animateTo(1f, tween(400)) }
        launch { loader.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
        delay(1200)
        finish()
    }

    val glowTop = if (c.isDark) BrandPalette.launchGlowDark else BrandPalette.launchGlowLight
    val ringColor = c.primary // #C2FF4D dark / #2BAA4A light
    val track = c.divider

    Box(
        Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Glowstreak" }
            .drawBehind {
                drawRect(c.background)
                drawRect(
                    Brush.radialGradient(
                        listOf(glowTop, c.background),
                        center = Offset(size.width / 2, size.height * 0.35f),
                        radius = size.height * 0.62f,
                    )
                )
            },
    ) {
        Column(Modifier.align(Alignment.Center).offset(y = (-40).dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                    val inset = stroke.width / 2
                    drawCircle(track, radius = size.minDimension / 2 - inset, style = stroke)
                    drawArc(
                        color = ringColor, startAngle = -90f, sweepAngle = 360f * ring.value,
                        useCenter = false, topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke.width, size.height - stroke.width), style = stroke,
                    )
                }
                val squircle = RoundedCornerShape(23)
                Box(
                    Modifier
                        .size(76.dp)
                        .graphicsLayer { scaleX = iconScale.value; scaleY = iconScale.value }
                        .glow(BrandPalette.lime.copy(alpha = 0.45f), 24.dp, 18.dp, enabled = c.isDark)
                        .clip(squircle)
                        .background(Brush.linearGradient(listOf(BrandPalette.lime, BrandPalette.mint))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_bolt), null, Modifier.size(38.dp), tint = BrandPalette.boltInk)
                }
            }
            Spacer(Modifier.height(28.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = (1f - textAlpha.value) * 10.dp.toPx()
                },
            ) {
                Text(
                    "Glowstreak",
                    style = AppTheme.textStyles.displayLarge.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.8).sp),
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Light up the days. The years follow.",
                    style = AppTheme.textStyles.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = c.textSecondary,
                )
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 60.dp)
                .size(96.dp, 4.dp)
                .clip(CircleShape)
                .background(track),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(loader.value)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(BrandPalette.lime, BrandPalette.mint))),
            )
        }
    }
}
