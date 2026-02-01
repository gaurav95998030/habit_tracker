package com.example.habit_tracker.commons.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VerticalSpace(size: Dp) {
    Spacer(modifier = Modifier.height(size))
}