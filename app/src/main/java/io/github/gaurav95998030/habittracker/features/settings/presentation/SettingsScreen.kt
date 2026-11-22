package io.github.gaurav95998030.habittracker.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import io.github.gaurav95998030.habittracker.features.widget.HabitWidgetProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenNotifications: () -> Unit = {},
) {
    val theme by viewModel.themePreference.collectAsStateWithLifecycle()
    val remindersOn by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    SettingsScreenContent(
        modifier = modifier,
        theme = theme,
        remindersOn = remindersOn,
        onThemeSelected = viewModel::onThemeSelected,
        onRemindersChanged = viewModel::onNotificationsEnabledChanged,
        onOpenNotifications = onOpenNotifications,
    )
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier,
    theme: ThemePreference,
    remindersOn: Boolean,
    onThemeSelected: (ThemePreference) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Text(
            text = "Settings",
            style = AppTheme.textStyles.titleLarge.copy(
                color = colors.textPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
        )

        SectionLabel("APPEARANCE")
        SettingsCard {
            ThemeRow(
                selected = theme,
                onSelect = onThemeSelected,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("NOTIFICATIONS")
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = "Habit reminders",
                subtitle = "Push at each habit's set time",
                trailing = {
                    Switch(
                        checked = remindersOn,
                        onCheckedChange = onRemindersChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.background,
                            checkedTrackColor = colors.primary,
                            checkedBorderColor = colors.primary,
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.surface,
                            uncheckedBorderColor = colors.divider,
                        ),
                    )
                },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Outlined.NotificationsActive,
                title = "Notification center",
                subtitle = "See every reminder in one place",
                trailing = { ChevronRight() },
                onClick = onOpenNotifications,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("HOME SCREEN")
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Widgets,
                title = "Add home widget",
                subtitle = "Track and tick off habits without opening the app",
                trailing = { ChevronRight() },
                onClick = { requestPinHabitWidget(context) },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("ABOUT")
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "What's new",
                subtitle = "Aurora 2.0",
                trailing = { ChevronRight() },
                onClick = {},
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Outlined.RestartAlt,
                title = "Replay onboarding",
                subtitle = null,
                trailing = { ChevronRight() },
                onClick = {},
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Outlined.Info,
                title = "Version",
                subtitle = "2.0.0 (build 36)",
                trailing = null,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        BrandFooter()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = AppTheme.textStyles.labelSmall.copy(
            color = AppTheme.colors.textTertiary,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface.copy(alpha = 0.7f))
            .border(
                width = 1.dp,
                color = colors.divider.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        content()
    }
}

private val ThemeChoices = listOf(
    Triple(ThemePreference.Light, "Light", Icons.Outlined.LightMode),
    Triple(ThemePreference.Dark, "Dark", Icons.Outlined.DarkMode),
    Triple(ThemePreference.System, "System", Icons.Outlined.PhoneAndroid),
)

@Composable
private fun ThemeRow(
    selected: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Theme",
            style = AppTheme.textStyles.bodyLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            ),
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(colors.background.copy(alpha = 0.5f))
                .border(1.dp, colors.divider.copy(alpha = 0.6f), CircleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ThemeChoices.forEach { (choice, label, icon) ->
                ThemePill(
                    label = label,
                    icon = icon,
                    isSelected = choice == selected,
                    onClick = { onSelect(choice) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val bg = if (isSelected) colors.primary else Color.Transparent
    val fg = if (isSelected) colors.onPrimary else colors.textSecondary

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = AppTheme.textStyles.labelLarge.copy(
                color = fg,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    trailing: (@Composable () -> Unit)?,
    onClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 18.dp, vertical = 14.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTheme.textStyles.bodyLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                ),
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.textStyles.bodyMedium.copy(
                        color = colors.textTertiary,
                    ),
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(contentAlignment = Alignment.Center) { trailing() }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = AppTheme.colors.divider.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 54.dp),
    )
}

@Composable
private fun ChevronRight() {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
        contentDescription = null,
        tint = AppTheme.colors.textTertiary,
        modifier = Modifier.size(22.dp),
    )
}

/**
 * Ask the launcher to pin the habit widget. Supported on API 26+ with a launcher that
 * allows programmatic pinning; on anything else the user adds it from the widget picker.
 */
private fun requestPinHabitWidget(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(AppWidgetManager::class.java) ?: return
    if (!manager.isRequestPinAppWidgetSupported) return
    manager.requestPinAppWidget(
        ComponentName(context, HabitWidgetProvider::class.java),
        null,
        null,
    )
}

@Composable
private fun BrandFooter() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(top = 16.dp, bottom = 8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "HABITFORGE",
            style = AppTheme.textStyles.labelMedium.copy(
                color = colors.textTertiary,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Forge the days. The years follow.",
            style = AppTheme.textStyles.bodyMedium.copy(
                color = colors.textTertiary,
            ),
        )
    }
}
