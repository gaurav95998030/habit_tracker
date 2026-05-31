package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.HabitCalendar
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.HabitHeatmapLinearStrip
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.ShowHabitTile
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.YearlyHabitHeatCalender
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable

fun HabitYearlyLists(
    viewModel: HabitViewModel,
    habits : List<Habit> = emptyList(),
    onEditHabit: (Habit) -> Unit = {},
    onDeleteHabit: (Habit) -> Unit = {}
){
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // 🚫 Block swipe-to-hide
            newValue != SheetValue.Hidden
        }
    )

    var isHabitDetailSheetOpen by remember { mutableStateOf(false) }
    var selectedHabitId by remember { mutableStateOf<String?>(null) }
    val selectedHabit = selectedHabitId?.let { id -> habits.firstOrNull { it.id == id } }

    // Deep link from a reminder notification: open the detail sheet for the requested
    // habit once it appears in the loaded list, then clear the pending flag.
    val pendingDetailHabitId = viewModel.uiState.collectAsState().value.pendingDetailHabitId
    LaunchedEffect(pendingDetailHabitId, habits) {
        android.util.Log.d(
            "HabitDeepLink",
            "yearly-list effect: pending=$pendingDetailHabitId habits=${habits.size}"
        )
        val pending = pendingDetailHabitId ?: return@LaunchedEffect
        val habit = habits.firstOrNull { it.id == pending } ?: return@LaunchedEffect
        selectedHabitId = habit.id
        isHabitDetailSheetOpen = true
        viewModel.consumePendingDetail()
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(){
                Spacer(modifier = Modifier.height(20.dp))
            }
            items(
                count = habits.size,
                key = { index -> habits[index].id }
            ) { index ->
                val habit = habits[index]
                SwipeToDeleteContainer(
                    onDelete = { onDeleteHabit(habit) }
                ) {
                    ShowHabitTile(
                        habit,
                        onToggleCompleted = { date ->
                            viewModel.updateHabit(habit.id, date)
                        },
                        onClick = {
                            selectedHabitId = habit.id
                            isHabitDetailSheetOpen = true
                        }
                    )
                }
            }

//            item {
//                HabitHeatmapLinearStrip(
//                    startDate = LocalDate.parse("2025-07-01"),
//                    endDate = LocalDate.parse("2026-02-01"),
//                    highlightedDates = listOf(
//                        LocalDate.parse("2025-08-01"),
//                        LocalDate.parse("2025-08-02"),
//                        LocalDate.parse("2025-08-03"),
//                        LocalDate.parse("2025-10-10")
//                    ),
//                    highlightColor = Color(0xFFCC6B6B)
//                )
//
//
//            }
        }
    }

    // Force-show whenever visibility flips on. ModalBottomSheet's auto-show races
    // with `confirmValueChange = { it != Hidden }` when the sheet enters the tree
    // in the same frame it becomes visible (cold-start deep-link path).
    LaunchedEffect(isHabitDetailSheetOpen) {
        if (isHabitDetailSheetOpen && !sheetState.isVisible) {
            sheetState.show()
        }
    }

    if (isHabitDetailSheetOpen && selectedHabit != null) {
        android.util.Log.d(
            "HabitDeepLink",
            "rendering detail sheet for ${selectedHabit.id}, sheetValue=${sheetState.currentValue}"
        )

        BackHandler(enabled = true) {
            isHabitDetailSheetOpen = false
            selectedHabitId = null
        }
        val blockSheetDrag = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset = available.copy(x = 0f)

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity = available.copy(x = 0f)
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                isHabitDetailSheetOpen = false
                selectedHabitId = null
            },
            sheetState = sheetState,
            modifier = Modifier.padding(0.dp),
            dragHandle = null
        ) {
            Box(modifier = Modifier.nestedScroll(blockSheetDrag)) {
                HabitDetailSheet(
                    habit = selectedHabit,
                    viewModel = viewModel,
                    onDismiss = {
                        isHabitDetailSheetOpen = false
                        selectedHabitId = null
                    },
                    onEditClick = {
                        val habitToEdit = selectedHabit
                        isHabitDetailSheetOpen = false
                        selectedHabitId = null
                        onEditHabit(habitToEdit)
                    },
                    onDeleteClick = {
                        val habitToDelete = selectedHabit
                        isHabitDetailSheetOpen = false
                        selectedHabitId = null
                        onDeleteHabit(habitToDelete)
                    }
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    var dismissed by remember { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                dismissed = true
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    LaunchedEffect(dismissed) {
        if (dismissed) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.error)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colors.onError,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        content()
    }
}
