package io.github.gaurav95998030.habittracker.features.widget.presentation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.widget.TodayLockScreen
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Asks before putting habit names on the lock screen. [onAllow] runs only once
 * notification permission is granted (requested here on Android 13+); [onDecline]
 * records the choice so the one-time prompt doesn't come back.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LockScreenConsentSheet(habits: List<Habit>, onAllow: () -> Unit, onDecline: () -> Unit) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onAllow() else {
            Toast.makeText(context, "Allow notifications to show today's habits on the lock screen.", Toast.LENGTH_LONG).show()
            onDecline()
        }
    }
    val allow = {
        if (TodayLockScreen.canNotify(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) onAllow()
        else permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    HfBottomSheet(onDismiss = onDecline) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            // The card on a faux lock screen.
            Column(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            if (c.isDark) listOf(Color(0xFF0D1C14), Color(0xFF050708)) else listOf(Color(0xFFDFEEDE), Color(0xFFF4F7F2))
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PhIcon(PhIcons.LockSimple, size = 16.dp, tint = c.textSecondary, weight = PhWeight.Fill)
                Text(
                    LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm", Locale.getDefault())),
                    style = AppTheme.textStyles.displayLarge.copy(fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1.5).sp),
                    color = c.textPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                )
                WidgetPreview(habits = habits, maxRows = 3)
            }

            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 22.dp),
            ) {
                Row(
                    Modifier.clip(CircleShape).background(c.primary.copy(alpha = 0.08f)).border(1.dp, c.primary.copy(alpha = 0.27f), CircleShape)
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PhIcon(PhIcons.LockSimple, size = 13.dp, tint = c.primary, weight = PhWeight.Fill)
                    Text("LOCK SCREEN", style = AppTheme.textStyles.labelSmall.copy(letterSpacing = 0.4.sp), color = c.primary)
                }
                Text(
                    "Today on your\nlock screen",
                    style = AppTheme.textStyles.titleLarge.copy(fontSize = 25.sp, lineHeight = 29.sp, letterSpacing = (-0.5).sp),
                    color = c.textPrimary,
                    modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    "See today’s progress and tick habits off without unlocking your phone. It shows as a quiet card — no sound or vibration.",
                    style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
                    color = c.textSecondary,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Row(
                    Modifier.padding(top = 18.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.warning.copy(alpha = 0.08f))
                        .border(1.dp, c.warning.copy(alpha = 0.27f), RoundedCornerShape(16.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                        PhIcon(PhIcons.Eye, size = 20.dp, tint = c.warning, weight = PhWeight.Bold)
                    }
                    Text(
                        "Your habit names will be visible to anyone who can see your lock screen. You can turn this off anytime in Settings → Privacy, or swipe the card away to hide it for the day.",
                        style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        color = c.textPrimary,
                    )
                }
            }

            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HfButton("Show on lock screen", onClick = allow, icon = PhIcons.LockSimple, size = HfButtonSize.Large, fullWidth = true)
                HfButton("Not now", onClick = onDecline, variant = HfButtonVariant.Ghost, fullWidth = true)
                Text(
                    "Off unless you turn it on.",
                    style = AppTheme.textStyles.caption,
                    fontWeight = FontWeight.Normal,
                    color = c.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
