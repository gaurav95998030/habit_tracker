package com.example.habit_tracker.features.habits.presentation.viewmodel

import com.example.habit_tracker.features.habits.domain.model.Habit
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.features.habits.HabitUtil
import com.example.habit_tracker.features.habits.domain.usecases.AddHabitUseCase
import com.example.habit_tracker.features.habits.domain.usecases.GetHabitsUseCase
import com.example.habit_tracker.features.habits.domain.usecases.ToggleHabitCompletionUseCase
import com.example.habit_tracker.features.habits.presentation.states.HabitFormState
import com.example.habit_tracker.features.habits.presentation.states.HabitState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val addHabitUseCase: AddHabitUseCase,
    private val getHabitsUseCase: GetHabitsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitState())
    val uiState = _uiState.asStateFlow()
    var formState by mutableStateOf(HabitFormState())
        private set

    fun addHabit(
        onHabitAdded: () -> Unit = {}
    ) {
        Log.d("HabitViewModel", "addHabit $formState")

        viewModelScope.launch {
           try{
               _uiState.update {
                   it.copy(
                       isAddingHabit = true
                   )
               }
               val habit = Habit(
                   id = HabitUtil.generateUniqueId(),
                   userId = "",
                   name = formState.name,
                   description = formState.description,
                   completedDates = emptyList(),
                   color = formState.color,
                   category = formState.category,
                   createdDate = Instant.now().toString(),
                   updatedDate = Instant.now().toString(),
                   maxStreak = 0,
                   isActive = true
               )
               addHabitUseCase.invoke(habit)
               _uiState.update {
                   it.copy(
                       isAddingHabit = false
                   )
               }
               formState = HabitFormState()
               //fetch habits again
               getHabits()
               onHabitAdded()
           }catch (e: Exception) {
               _uiState.update {
                   it.copy(
                       isAddingHabit = false
                   )
               }
               Log.e("HabitViewModel", "Error adding habit", e)
           }
        }
        //show condition
    }


    fun getHabits(){
        try{
           viewModelScope.launch {
               _uiState.update {
                   it.copy(
                       isLoading = true
                   )
               }
               val habits = getHabitsUseCase.invoke()
               Log.d("HabitViewModel", "getHabits $habits")
               _uiState.update {
                   it.copy(
                       habits = habits,
                       isLoading = false
                   )
               }
           }
        }catch (e: Exception){
            Log.e("HabitViewModel", "Error getting habits", e)
            _uiState.update {
                it.copy(
                    habits = emptyList(),
                    isLoading = false
                )
            }

        }
    }


    fun updateHabit(habitId: String, date: LocalDate) {
        val dateStr = date.toString()

        viewModelScope.launch {
            try {
                var updatedHabit: Habit? = null

                _uiState.update { state ->

                    val updatedHabits = state.habits
                        ?.map { habit ->

                            if (habit.id != habitId) {
                                return@map habit
                            }

                            // toggle completion date
                            val updatedDates =
                                if (habit.completedDates.contains(dateStr)) {
                                    habit.completedDates - dateStr   // remove
                                } else {
                                    habit.completedDates + dateStr   // add
                                }

                            val newHabit = habit.copy(
                                completedDates = updatedDates
                            )

                            // capture updated habit to pass to use case later
                            updatedHabit = newHabit

                            newHabit
                        }
                        ?: emptyList()

                    state.copy(habits = updatedHabits)
                }

                // call use-case *after* state update
                updatedHabit?.let {
                    toggleHabitCompletionUseCase.invoke(it)
                }

            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error updating habit", e)
            }
        }
    }







    fun onNameChanged(value: String) {
        if(value.trim().isEmpty()){
            formState = formState.copy(nameError = "Name cannot be empty", isValid = false)
            return
        }else{
            formState = formState.copy(
                name = value,
                isValid = value.isNotBlank()
            )
        }

    }

    fun onDescriptionChanged(value: String) {
        if(value.trim().isEmpty()){
            formState = formState.copy(descriptionError = "Description cannot be empty", isValid = false)
            return
        }else{
            formState = formState.copy(
                description = value,
                isValid = value.isNotBlank()
            )
        }
    }

    fun onCategoryChanged(value: String) {
        if(value.trim().isEmpty()){
            formState = formState.copy(categoryError = "Category cannot be empty", isValid = false)
            return
        }else{
            formState = formState.copy(
                category = value,
                isValid = value.isNotBlank()
            )
        }
    }

    fun onColorSelected(value: String) {
        if(value.trim().isEmpty()){
            formState = formState.copy(colorError = "Color cannot be empty", isValid = false)
            return
        }else{
            formState = formState.copy(color = value, isValid = value.isNotBlank())
        }

    }



}
