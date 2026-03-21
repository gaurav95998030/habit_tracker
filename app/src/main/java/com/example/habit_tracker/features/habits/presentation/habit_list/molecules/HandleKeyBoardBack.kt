package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun HandleKeyboardBack() {
    val focusManager = LocalFocusManager.current
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    BackHandler(enabled = imeVisible) {
        focusManager.clearFocus()
    }
}