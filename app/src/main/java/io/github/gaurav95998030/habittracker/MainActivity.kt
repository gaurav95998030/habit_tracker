package io.github.gaurav95998030.habittracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.gaurav95998030.habittracker.features.onboarding.presentation.OnboardingResult
import io.github.gaurav95998030.habittracker.features.onboarding.presentation.OnboardingScreen
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.reminders.ReminderConstants
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import io.github.gaurav95998030.habittracker.features.settings.presentation.SettingsViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.Habit_TrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    // Android 13+ needs the POST_NOTIFICATIONS runtime grant before any reminder can
    // show. Without it the alarm fires but the notification is silently dropped, so we
    // ask once on launch (the reminder picker also asks when a reminder is enabled).
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        // Handle a notification tap that cold-started us.
        handleDeepLink(intent)
        setContent {
            // Theme preference is persisted in DataStore via SettingsRepository.
            // collectAsStateWithLifecycle emits the StateFlow's current value
            // immediately (default Dark for fresh installs, then the persisted
            // value once it loads), so the whole tree re-themes on toggle.
            val themePref by settingsViewModel.themePreference.collectAsStateWithLifecycle()
            val isDark = when (themePref) {
                ThemePreference.Light -> false
                ThemePreference.Dark -> true
                ThemePreference.System -> isSystemInDarkTheme()
            }
            val onboarded by settingsViewModel.onboardingCompleted.collectAsStateWithLifecycle()
            // Captured on the first resolved value: only launches that *start*
            // onboarded may auto-show the widget nudge.
            var startedOnboarded by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(onboarded) { if (startedOnboarded == null && onboarded != null) startedOnboarded = onboarded }

            // Status/nav bar icons follow the in-app theme, not the system one.
            val view = LocalView.current
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }

            Habit_TrackerTheme(darkTheme = isDark) {
                Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
                    when (onboarded) {
                        null -> Unit // first DataStore read in flight — blank, no flash
                        false -> OnboardingScreen(onFinish = ::finishOnboarding)
                        true -> MainScreen(
                            habitViewModel = viewModel,
                            settingsViewModel = settingsViewModel,
                            widgetPromoEligible = startedOnboarded == true,
                        )
                    }
                }
            }
        }
    }

    private fun finishOnboarding(result: OnboardingResult) {
        settingsViewModel.completeOnboarding(result.name, result.categories)
        viewModel.addOnboardingHabit(
            name = result.habitName,
            category = result.habitCategory,
            colorHex = result.habitColorHex,
            icon = result.habitIcon,
        )
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // launchMode=singleTop routes subsequent taps through here while we're alive.
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent == null) {
            Log.d("HabitDeepLink", "handleDeepLink: intent=null")
            return
        }
        // Try the explicit extra first, then fall back to the URI path. Some OEMs
        // strip / dedupe extras when restoring a task, but data URIs survive.
        val fromExtra = intent.getStringExtra(ReminderConstants.EXTRA_OPEN_HABIT_DETAIL)
        val fromUri = intent.data
            ?.takeIf {
                it.scheme == ReminderConstants.DEEPLINK_SCHEME &&
                    it.host == ReminderConstants.DEEPLINK_HOST
            }
            ?.lastPathSegment
        val habitId = fromExtra ?: fromUri
        Log.d(
            "HabitDeepLink",
            "handleDeepLink: action=${intent.action} data=${intent.data} extra=$fromExtra uri=$fromUri resolved=$habitId"
        )
        if (habitId.isNullOrBlank()) return
        viewModel.openHabitDetail(habitId)
        // Clear so we don't re-fire on configuration changes / re-creation.
        intent.removeExtra(ReminderConstants.EXTRA_OPEN_HABIT_DETAIL)
        intent.data = null
    }
}
