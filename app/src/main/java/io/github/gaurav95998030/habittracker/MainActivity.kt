package io.github.gaurav95998030.habittracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.HabitListScreen
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.reminders.ReminderConstants
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.Habit_TrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Handle a notification tap that cold-started us.
        handleDeepLink(intent)
        setContent {
            Habit_TrackerTheme {
                HabitListScreen(
                    viewModel = viewModel,
                )
            }
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
