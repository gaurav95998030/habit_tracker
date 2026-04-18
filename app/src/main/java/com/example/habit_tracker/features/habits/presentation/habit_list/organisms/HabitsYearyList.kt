package com.example.habit_tracker.features.habits.presentation.habit_list.organisms

import com.example.habit_tracker.features.habits.domain.model.Habit
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitCalendar
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitHeatmapLinearStrip
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.ShowHabitTile
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.YearlyHabitHeatCalender
import com.example.habit_tracker.features.habits.presentation.viewmodel.HabitViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable

fun HabitYearlyLists(
    viewModel: HabitViewModel,
    habits : List<Habit> = emptyList()
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
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .statusBarsPadding()
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
                ShowHabitTile(
                    habits[index],
                    onToggleCompleted = { date->
                        viewModel.updateHabit(habits[index].id, date)
                    },
                    onClick = {
                        selectedHabitId = habits[index].id
                        isHabitDetailSheetOpen = true
                    }
                )
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

    if (isHabitDetailSheetOpen && selectedHabit != null) {

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
                    }
                )
            }
        }

    }
}