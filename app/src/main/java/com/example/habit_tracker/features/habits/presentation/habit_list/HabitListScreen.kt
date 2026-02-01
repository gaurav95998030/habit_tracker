package com.example.habit_tracker.features.habits.presentation.habit_list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.features.habits.presentation.habit_list.organisms.AddHabitSheet
import com.example.habit_tracker.features.habits.presentation.habit_list.organisms.HabitYearlyLists
import com.example.habit_tracker.features.habits.presentation.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
){
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showSheet by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.getHabits()
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                 showSheet = true;
                }

            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { _ ->
        if(uiState.value.isLoading){
            Text("Habits loading")
        }else{
            HabitYearlyLists(
                viewModel,
                uiState.value.habits?:emptyList()
            )
        }

    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            modifier = Modifier.padding(0.dp)

        ) {
            AddHabitSheet(
                sheetState = sheetState,
                viewModel = viewModel,
                onDismiss = { showSheet = false }
            )
        }
    }
}