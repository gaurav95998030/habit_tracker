package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyLevel
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.DeleteStackUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.GetStarterPlansUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StarterPlan
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.LogMissUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.ObserveEnergyUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.ObserveMissLogsUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.ObserveStacksUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.SaveStackUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.SetEnergyUseCase
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Miss reasons, energy check-ins, habit stacks and starter plans. Activity-scoped
 * (like HabitViewModel) so the "log yesterday's misses" deep link reaches the UI.
 * Habit rows themselves are still created/updated through HabitViewModel.
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MotivationViewModel @Inject constructor(
    observeMissLogs: ObserveMissLogsUseCase,
    private val logMissUseCase: LogMissUseCase,
    observeEnergy: ObserveEnergyUseCase,
    private val setEnergyUseCase: SetEnergyUseCase,
    observeStacks: ObserveStacksUseCase,
    private val saveStackUseCase: SaveStackUseCase,
    private val deleteStackUseCase: DeleteStackUseCase,
    private val settingsRepository: SettingsRepository,
    private val getStarterPlans: GetStarterPlansUseCase,
) : ViewModel() {

    private val today = LocalDate.now()

    /** Last 90 days of logged misses (what "Why you miss" analyses). */
    val missLogs: StateFlow<List<MissLog>> =
        observeMissLogs(today.minusDays(90).toString()).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Last 35 days of energy ratings. */
    val energy: StateFlow<List<EnergyCheckin>> =
        observeEnergy(today.minusDays(35).toString()).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val stacks: StateFlow<List<HabitStack>> =
        observeStacks().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activePlans: StateFlow<Map<String, String>> =
        settingsRepository.activePlans.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _plans = MutableStateFlow<List<StarterPlan>?>(null)

    /** Starter plans; `null` while loading. Comes from the repository (bundled JSON today, API later). */
    val plans: StateFlow<List<StarterPlan>?> = _plans.asStateFlow()

    init {
        viewModelScope.launch { _plans.value = getStarterPlans() }
    }

    private val _missSheetRequested = MutableStateFlow(false)

    /** Set by the "you missed … yesterday" notification; the UI opens the reason sheet. */
    val missSheetRequested: StateFlow<Boolean> = _missSheetRequested.asStateFlow()

    fun requestMissSheet() { _missSheetRequested.value = true }
    fun consumeMissSheet() { _missSheetRequested.value = false }

    /** Log (or with null, clear) why [habitId] was missed on [date]. */
    fun logMiss(habitId: String, date: String, reason: MissReason?) {
        viewModelScope.launch { logMissUseCase(habitId, date, reason) }
    }

    /** Today's energy; tapping the selected level again clears it. */
    fun setTodayEnergy(level: EnergyLevel?) {
        viewModelScope.launch { setEnergyUseCase(LocalDate.now().toString(), level) }
    }

    /** Create (null [id]) or update a stack. Returns the stack id. */
    fun saveStack(id: String?, name: String, steps: List<StackStep>): String {
        val stackId = id ?: HabitUtil.generateUniqueId()
        val created = stacks.value.firstOrNull { it.id == stackId }?.createdDate ?: Instant.now().toString()
        viewModelScope.launch { saveStackUseCase(stackId, name.trim().ifBlank { "My stack" }, created, steps) }
        return stackId
    }

    fun deleteStack(id: String) {
        viewModelScope.launch { deleteStackUseCase(id) }
    }

    fun startPlan(planId: String) {
        viewModelScope.launch { settingsRepository.setPlan(planId, LocalDate.now().toString()) }
    }

    fun endPlan(planId: String) {
        viewModelScope.launch { settingsRepository.setPlan(planId, null) }
    }
}
