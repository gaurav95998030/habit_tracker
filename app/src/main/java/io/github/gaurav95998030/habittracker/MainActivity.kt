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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
            Habit_TrackerTheme(darkTheme = isDark) {
                MainScreen(habitViewModel = viewModel)
            }
        }
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        style = AppTheme.textStyles.titleLarge,
        color = AppTheme.colors.textPrimary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Habit_TrackerTheme {
        Greeting("Habit Tracker")
    }
}
