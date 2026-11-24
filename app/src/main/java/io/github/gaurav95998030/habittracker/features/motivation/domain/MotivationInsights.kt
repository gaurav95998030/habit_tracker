package io.github.gaurav95998030.habittracker.features.motivation.domain

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

data class ReasonCount(val reason: MissReason, val count: Int)

/** Suggestion when one reason clusters on one weekday. [habit] is the habit missed most that day. */
data class MissSuggestion(val reason: MissReason, val day: DayOfWeek, val habit: Habit?)

data class MissPatterns(
    val total: Int,
    val reasons: List<ReasonCount>,
    /** Monday-first counts. */
    val byWeekday: List<Int>,
    val topDayIndex: Int,
    val suggestion: MissSuggestion?,
)

data class EnergyDay(val date: LocalDate, val checkin: EnergyCheckin?, val done: Int, val total: Int)

data class EnergyInsight(
    val week: List<EnergyDay>,
    /** Average completion % on high (4–5) and low (1–2) energy days; null without ≥2 such days. */
    val highPercent: Int?,
    val lowPercent: Int?,
) {
    val liftPercent: Int? get() =
        if (highPercent != null && lowPercent != null && lowPercent > 0) ((highPercent - lowPercent) * 100f / lowPercent).roundToInt() else null
}

data class StackProgress(
    val stack: HabitStack,
    /** Steps whose habit still exists, in order. */
    val steps: List<Pair<StackStep, Habit>>,
    /** How many leading steps are done today. */
    val done: Int,
) {
    val next: Pair<StackStep, Habit>? get() = steps.getOrNull(done)
    val complete: Boolean get() = steps.isNotEmpty() && done >= steps.size
}

@RequiresApi(Build.VERSION_CODES.O)
object MotivationInsights {

    /** Active habits that existed yesterday, weren't done, and have no reason logged yet. */
    fun missedYesterday(habits: List<Habit>, logs: List<MissLog>, today: LocalDate = LocalDate.now()): List<Habit> {
        val y = today.minusDays(1)
        val logged = logs.filter { it.date == y.toString() }.map { it.habitId }.toSet()
        return habits.filter { h ->
            val created = HabitStats.createdOn(h)
            h.isActive && (created == null || !created.isAfter(y)) && !HabitStats.isDoneOn(h, y) && h.id !in logged
        }
    }

    fun missPatterns(habits: List<Habit>, logs: List<MissLog>): MissPatterns {
        val known = habits.associateBy { it.id }
        val valid = logs.filter { it.habitId in known }
        val reasons = MissReason.entries.map { r -> ReasonCount(r, valid.count { it.reason == r }) }
            .filter { it.count > 0 }.sortedByDescending { it.count }
        val dates = valid.mapNotNull { l -> runCatching { LocalDate.parse(l.date) }.getOrNull()?.let { l to it } }
        val byDay = IntArray(7).also { a -> dates.forEach { (_, d) -> a[d.dayOfWeek.value - 1]++ } }.toList()
        val topDay = byDay.indices.maxByOrNull { byDay[it] } ?: 0

        // Suggest only with a real cluster: top reason has ≥3 logs and ≥2 of them on one weekday.
        val suggestion = reasons.firstOrNull()?.takeIf { it.count >= 3 }?.let { top ->
            val ofTop = dates.filter { it.first.reason == top.reason }
            val dayCounts = ofTop.groupingBy { it.second.dayOfWeek }.eachCount()
            val (day, n) = dayCounts.maxByOrNull { it.value } ?: return@let null
            if (n < 2) return@let null
            val habitId = ofTop.filter { it.second.dayOfWeek == day }.groupingBy { it.first.habitId }.eachCount().maxByOrNull { it.value }?.key
            MissSuggestion(top.reason, day, habitId?.let { known[it] })
        }
        return MissPatterns(valid.size, reasons, byDay, topDay, suggestion)
    }

    fun energy(habits: List<Habit>, checkins: List<EnergyCheckin>, today: LocalDate = LocalDate.now()): EnergyInsight {
        val active = habits.filter { it.isActive }
        val sets = active.associate { it.id to HabitStats.completedSet(it) }
        val byDate = checkins.associateBy { it.date }
        fun day(d: LocalDate) = EnergyDay(d, byDate[d.toString()], active.count { d in sets[it.id].orEmpty() }, active.size)
        val window = (0L until 30L).map { day(today.minusDays(it)) }.filter { it.checkin != null && it.total > 0 }
        fun avg(levels: IntRange): Int? = window.filter { it.checkin!!.level.level in levels }
            .takeIf { it.size >= 2 }?.map { it.done * 100f / it.total }?.average()?.roundToInt()
        return EnergyInsight(
            week = (6 downTo 0).map { day(today.minusDays(it.toLong())) },
            highPercent = avg(4..5),
            lowPercent = avg(1..2),
        )
    }

    fun stackProgress(stack: HabitStack, habits: List<Habit>, today: LocalDate = LocalDate.now()): StackProgress {
        val byId = habits.associateBy { it.id }
        val steps = stack.steps.mapNotNull { s -> byId[s.habitId]?.let { s to it } }
        val done = steps.takeWhile { (_, h) -> HabitStats.isDoneOn(h, today) }.size
        return StackProgress(stack, steps, done)
    }
}
