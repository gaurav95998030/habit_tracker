package io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.AddHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.DeleteHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.GetHabitsUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.ToggleHabitCompletionUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.UpdateHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HabitFormState
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HabitState
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HeatmapViewMode
import io.github.gaurav95998030.habittracker.features.reminders.HabitReminderScheduler
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import io.github.gaurav95998030.habittracker.features.widget.HabitWidgetProvider
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val reminderScheduler: HabitReminderScheduler,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    /** Keep the home-screen widget in sync after any change to the habit list. */
    private fun refreshWidget() = HabitWidgetProvider.refresh(appContext)

    private val _uiState = MutableStateFlow(HabitState())
    val uiState = _uiState.asStateFlow()
    private val _isReady = MutableStateFlow(false)

    /** True once the first habit load from Room has finished (holds the system splash). */
    val isReady = _isReady.asStateFlow()

    var formState by mutableStateOf(HabitFormState())
        private set

    private var editingHabitSnapshot: Habit? = null

    init {
        // Activity-scoped ViewModel: load once on construction so re-entering
        // the Home tab doesn't re-trigger the shimmer. saveHabit/restoreHabit
        // already call getHabits() to refresh after mutations, and PTR refreshes
        // on demand.
        getHabits()
    }

    fun startEditingHabit(habit: Habit) {
        editingHabitSnapshot = habit
        formState = HabitFormState(
            name = habit.name,
            description = habit.description,
            category = habit.category,
            color = habit.color,
            isValid = habit.name.isNotBlank(),
            editingHabitId = habit.id,
            reminderEnabled = habit.reminderEnabled,
            reminderTime = habit.reminderTime ?: HabitCatalog.DEFAULT_REMINDER_TIME
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
                val reminderEnabled = formState.reminderEnabled && formState.reminderTime != null
                val reminderTime = if (reminderEnabled) formState.reminderTime else null

                val savedHabit: Habit = if (editingId != null && snapshot != null) {
                    val updated = snapshot.copy(
                        name = formState.name,
                        description = formState.description,
                        color = formState.color,
                        category = formState.category,
                        updatedDate = Instant.now().toString(),
                        reminderEnabled = reminderEnabled,
                        reminderTime = reminderTime
                    )
                    updateHabitUseCase.invoke(updated)
                    updated
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
                        isActive = true,
                        reminderEnabled = reminderEnabled,
                        reminderTime = reminderTime
                    )
                    addHabitUseCase.invoke(habit)
                    habit
                }

                // Cancel any prior alarm for this habit, then re-schedule if enabled.
                // The master switch in Settings gates the (re)schedule — if the
                // user has notifications off globally, we still cancel so a
                // previously-armed alarm stops firing, but we don't re-arm.
                reminderScheduler.cancel(savedHabit.id)
                val masterEnabled = settingsRepository.notificationsEnabled.first()
                if (masterEnabled && savedHabit.isActive && savedHabit.reminderEnabled && savedHabit.reminderTime != null) {
                    reminderScheduler.schedule(savedHabit)
                }

                _uiState.update { it.copy(isAddingHabit = false) }
                resetForm()
                getHabits()
                refreshWidget()
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
               try {
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
               } catch (e: Exception) {
                   Log.e("HabitViewModel", "Error getting habits", e)
                   _uiState.update { it.copy(isLoading = false) }
               } finally {
                   _isReady.value = true
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

    // Suspending refresh used by pull-to-refresh. Does not flip `isLoading` so the
    // shimmer placeholder doesn't replace the list while the PTR indicator is shown.
    suspend fun refreshHabits() {
        try {
            val habits = getHabitsUseCase.invoke()
            _uiState.update { it.copy(habits = habits) }
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Error refreshing habits", e)
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
                    refreshWidget()
                }

            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error updating habit", e)
            }
        }
    }

    /** Check-circle on a tile / notification center "Done": toggle today. */
    fun toggleToday(habitId: String) = updateHabit(habitId, LocalDate.now())

    /** Month calendar in the detail sheet: toggle any past/present day ("yyyy-MM-dd"). */
    fun toggleDay(habitId: String, date: String) {
        val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return
        if (parsed.isAfter(LocalDate.now())) return
        updateHabit(habitId, parsed)
    }

    /**
     * Pause / resume. Pausing keeps history but drops the habit out of today's
     * progress, insights and reminders (its alarm is cancelled); resuming re-arms it.
     */
    fun setHabitActive(habit: Habit, active: Boolean) {
        val updated = habit.copy(isActive = active, updatedDate = Instant.now().toString())
        _uiState.update { state ->
            state.copy(habits = state.habits?.map { if (it.id == habit.id) updated else it })
        }
        viewModelScope.launch {
            try {
                updateHabitUseCase.invoke(updated)
                reminderScheduler.cancel(updated.id)
                val masterEnabled = settingsRepository.notificationsEnabled.first()
                if (active && masterEnabled && updated.reminderEnabled && updated.reminderTime != null) {
                    reminderScheduler.schedule(updated)
                }
                refreshWidget()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error updating habit active state", e)
            }
        }
    }

    /** Creates the first habit picked in onboarding (no reminder — set it later). */
    fun addOnboardingHabit(name: String, category: String, colorHex: String, icon: String?) {
        viewModelScope.launch {
            try {
                val now = Instant.now().toString()
                addHabitUseCase.invoke(
                    Habit(
                        id = HabitUtil.generateUniqueId(),
                        userId = "",
                        name = name,
                        description = "",
                        completedDates = emptyList(),
                        color = colorHex,
                        category = category,
                        createdDate = now,
                        updatedDate = now,
                        maxStreak = 0,
                        isActive = true,
                        reminderEnabled = false,
                        reminderTime = null,
                        icon = icon,
                    )
                )
                getHabits()
                refreshWidget()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error adding onboarding habit", e)
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
                reminderScheduler.cancel(habit.id)
                refreshWidget()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error deleting habit", e)
            }
        }
    }

    fun restoreHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                addHabitUseCase.invoke(habit)
                val masterEnabled = settingsRepository.notificationsEnabled.first()
                if (masterEnabled && habit.isActive && habit.reminderEnabled && habit.reminderTime != null) {
                    reminderScheduler.schedule(habit)
                }
                getHabits()
                refreshWidget()
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

    fun onReminderEnabledChanged(enabled: Boolean) {
        formState = formState.copy(
            reminderEnabled = enabled,
            // default to 9:00 AM when first enabling without an existing time
            reminderTime = if (enabled && formState.reminderTime == null) HabitCatalog.DEFAULT_REMINDER_TIME else formState.reminderTime
        )
    }

    /** Quick-time chip in the add sheet ("HH:mm"). */
    fun onReminderTimeSelected(time: String) {
        formState = formState.copy(reminderEnabled = true, reminderTime = time)
    }

    fun onReminderTimeChanged(hour: Int, minute: Int) {
        val formatted = "%02d:%02d".format(hour, minute)
        formState = formState.copy(
            reminderEnabled = true,
            reminderTime = formatted
        )
    }

    /**
     * Called from MainActivity when a reminder notification is tapped. We stash the
     * id in state; the habit list screen opens the detail sheet once the habit shows
     * up in the loaded list, then calls [consumePendingDetail] to clear it.
     */
    fun openHabitDetail(habitId: String) {
        Log.d("HabitDeepLink", "openHabitDetail: $habitId")
        _uiState.update { it.copy(pendingDetailHabitId = habitId) }
        // Ensure the list is loaded so the detail sheet has something to render.
        if (_uiState.value.habits.isNullOrEmpty()) {
            getHabits()
        }
    }

    /** Ask Home to open the edit sheet for [habitId] (consumed by HabitListScreen). */
    fun requestEditHabit(habitId: String) {
        _uiState.update { it.copy(pendingEditHabitId = habitId) }
    }

    fun consumePendingEdit() {
        _uiState.update { it.copy(pendingEditHabitId = null) }
    }

    /**
     * Adds several habits at once (starter plans). Reminders start off; icons, colors
     * and categories come from the plan.
     */
    fun addHabits(drafts: List<HabitDraft>) {
        if (drafts.isEmpty()) return
        viewModelScope.launch {
            try {
                val now = Instant.now().toString()
                drafts.forEach { d ->
                    addHabitUseCase.invoke(
                        Habit(
                            id = HabitUtil.generateUniqueId(), userId = "", name = d.name, description = "",
                            completedDates = emptyList(), color = d.colorHex, category = d.category,
                            createdDate = now, updatedDate = now, maxStreak = 0, isActive = true,
                            reminderEnabled = false, reminderTime = null, icon = d.icon,
                        )
                    )
                }
                getHabits()
                refreshWidget()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error adding habits", e)
            }
        }
    }

    fun consumePendingDetail() {
        _uiState.update { it.copy(pendingDetailHabitId = null) }
    }

    fun toggleHeatmapViewMode() {
        _uiState.update {
            it.copy(
                heatmapViewMode = if (it.heatmapViewMode == HeatmapViewMode.Week)
                    HeatmapViewMode.Year else HeatmapViewMode.Week
            )
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

}

/** Minimal spec for bulk-created habits ([colorHex] in storage form). */
data class HabitDraft(val name: String, val category: String, val colorHex: String, val icon: String?)
