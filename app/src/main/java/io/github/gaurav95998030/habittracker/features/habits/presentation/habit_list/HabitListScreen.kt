package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.commons.ui.PullToRefresh
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.ShimmerHabitList
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms.AddHabitSheet
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms.HabitYearlyLists
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppColors
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = AppTheme.colors

    val onDeleteHabit: (Habit) -> Unit = { habit ->
        viewModel.deleteHabit(habit)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val autoDismiss = launch {
                delay(3000)
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            val result = snackbarHostState.showSnackbar(
                message = "\"${habit.name}\" deleted",
                actionLabel = "Undo",
                withDismissAction = false,
                duration = SnackbarDuration.Indefinite
            )
            autoDismiss.cancel()
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreHabit(habit)
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.getHabits()
    }
    Scaffold(

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetForm()
                    showSheet = true
                }

            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.surface,
                    contentColor = colors.textPrimary,
                    actionColor = colors.error
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if(uiState.value.isLoading){
            ShimmerHabitList()
        }else{
            PullToRefresh(
                onRefresh = { viewModel.refreshHabits() },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                HabitYearlyLists(
                    viewModel = viewModel,
                    habits = uiState.value.habits ?: emptyList(),
                    onEditHabit = { habit ->
                        viewModel.startEditingHabit(habit)
                        showSheet = true
                    },
                    onDeleteHabit = onDeleteHabit
                )
            }
        }

    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                viewModel.resetForm()
            },
            dragHandle = { SheetDragHandle() },
            sheetState = sheetState,
            containerColor = Color(0xFF000000),
            modifier = Modifier.padding(0.dp),
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
        ) {
            AddHabitSheet(
                sheetState = sheetState,
                viewModel = viewModel,
                onDismiss = {
                    showSheet = false
                    viewModel.resetForm()
                }
            )
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF3A3A3A))
        )
    }
}
