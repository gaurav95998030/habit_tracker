package com.example.habit_tracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habit_tracker.features.habits.presentation.habit_list.HabitListScreen
import com.example.habit_tracker.features.habits.presentation.viewmodel.HabitViewModel
import com.example.habit_tracker.ui.theme.AppTheme
import com.example.habit_tracker.ui.theme.Habit_TrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Habit_TrackerTheme {
                val viewModel: HabitViewModel = hiltViewModel()
                HabitListScreen(
                    viewModel = viewModel,
                )
            }
        }
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