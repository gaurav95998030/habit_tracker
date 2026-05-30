package io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.AddHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.DeleteHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.GetHabitsUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.ToggleHabitCompletionUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.UpdateHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HabitFormState
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HabitState
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
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitState())
    val uiState = _uiState.asStateFlow()
    var formState by mutableStateOf(HabitFormState())
        private set

    private var editingHabitSnapshot: Habit? = null

    fun startEditingHabit(habit: Habit) {
        editingHabitSnapshot = habit
        formState = HabitFormState(
            name = habit.name,
            description = habit.description,
            category = habit.category,
            color = habit.color,
            isValid = habit.name.isNotBlank(),
            editingHabitId = habit.id
        )
    }

    fun resetForm() {
        editingHabitSnapshot = null
        formState = HabitFormState()
    }

    fun saveHabit(
        onSaved: () -> Unit = {}
    ) {
        Log.d("HabitViewModel", "saveHabit $formState")

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAddingHabit = true) }

                val editingId = formState.editingHabitId
                val snapshot = editingHabitSnapshot
                if (editingId != null && snapshot != null) {
                    val updated = snapshot.copy(
                        name = formState.name,
                        description = formState.description,
                        color = formState.color,
                        category = formState.category,
                        updatedDate = Instant.now().toString()
                    )
                    updateHabitUseCase.invoke(updated)
                } else {
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
                }

                _uiState.update { it.copy(isAddingHabit = false) }
                resetForm()
                getHabits()
                onSaved()
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingHabit = false) }
                Log.e("HabitViewModel", "Error saving habit", e)
            }
        }
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

    fun deleteHabit(habit: Habit) {
        _uiState.update { state ->
            state.copy(habits = state.habits?.filterNot { it.id == habit.id } ?: emptyList())
        }
        viewModelScope.launch {
            try {
                deleteHabitUseCase.invoke(habit.id)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error deleting habit", e)
            }
        }
    }

    fun restoreHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                addHabitUseCase.invoke(habit)
                getHabits()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error restoring habit", e)
            }
        }
    }





    fun onNameChanged(value: String) {
        formState = formState.copy(
            name = value,
            nameError = if (value.trim().isEmpty()) "Name cannot be empty" else "",
            isValid = value.isNotBlank()
        )
    }

    fun onDescriptionChanged(value: String) {
        formState = formState.copy(
            description = value,
            descriptionError = ""
        )
    }

    fun onCategoryChanged(value: String) {
        formState = formState.copy(
            category = value,
            categoryError = ""
        )
    }

    fun onColorSelected(value: String) {
        formState = formState.copy(
            color = value,
            colorError = ""
        )
    }



}
