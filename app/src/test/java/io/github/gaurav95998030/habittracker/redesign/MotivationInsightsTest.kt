package io.github.gaurav95998030.habittracker.redesign

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.MotivationInsights
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyLevel
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class MotivationInsightsTest {

    private val today = LocalDate.of(2026, 11, 24) // Tuesday

    private fun habit(id: String, done: List<LocalDate> = emptyList(), active: Boolean = true, createdDaysAgo: Long = 60) = Habit(
        id = id, userId = "", name = "H$id", description = "", completedDates = done.map { it.toString() },
        color = "#FFB6FF3C", category = "Body",
        createdDate = today.minusDays(createdDaysAgo).atStartOfDay(ZoneId.systemDefault()).toInstant().toString(),
        updatedDate = "", maxStreak = 0, isActive = active,
    )

    @Test fun missedYesterday_excludesDoneLoggedPausedAndNew() {
        val y = today.minusDays(1)
        val habits = listOf(
            habit("missed"),
            habit("done", listOf(y)),
            habit("logged"),
            habit("paused", active = false),
            habit("new", createdDaysAgo = 0),
        )
        val logs = listOf(MissLog("logged", y.toString(), MissReason.Busy))
        assertEquals(listOf("missed"), MotivationInsights.missedYesterday(habits, logs, today).map { it.id })
    }

    @Test fun missPatterns_countsReasonsWeekdaysAndSuggests() {
        val thursdays = listOf(LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 5))
        val logs = thursdays.map { MissLog("run", it.toString(), MissReason.Busy) } +
            MissLog("read", "2026-11-17", MissReason.Travel) +
            MissLog("gone", "2026-11-16", MissReason.Sick) // habit no longer exists → ignored
        val p = MotivationInsights.missPatterns(listOf(habit("run"), habit("read")), logs)
        assertEquals(4, p.total)
        assertEquals(MissReason.Busy, p.reasons.first().reason)
        assertEquals(3, p.byWeekday[3]) // Thursday (Monday-first)
        assertEquals(3, p.topDayIndex)
        assertEquals(DayOfWeek.THURSDAY, p.suggestion!!.day)
        assertEquals("run", p.suggestion!!.habit!!.id)
    }

    @Test fun missPatterns_noSuggestionWithoutCluster() {
        val logs = listOf("2026-11-16", "2026-11-17", "2026-11-18").map { MissLog("a", it, MissReason.Busy) }
        assertNull(MotivationInsights.missPatterns(listOf(habit("a")), logs).suggestion) // 3 busy, all different days
    }

    @Test fun energy_liftBetweenHighAndLowDays() {
        val d = { n: Long -> today.minusDays(n) }
        // a & b both done on high-energy days 1,2; only a on low days 3,4.
        val a = habit("a", listOf(d(1), d(2), d(3), d(4)))
        val b = habit("b", listOf(d(1), d(2)))
        val checkins = listOf(
            EnergyCheckin(d(1).toString(), EnergyLevel.Charged), EnergyCheckin(d(2).toString(), EnergyLevel.Good),
            EnergyCheckin(d(3).toString(), EnergyLevel.Low), EnergyCheckin(d(4).toString(), EnergyLevel.Drained),
        )
        val e = MotivationInsights.energy(listOf(a, b), checkins, today)
        assertEquals(100, e.highPercent)
        assertEquals(50, e.lowPercent)
        assertEquals(100, e.liftPercent)
        assertEquals(7, e.week.size)
        assertEquals(today, e.week.last().date)
    }

    @Test fun energy_needsTwoDaysPerBucket() {
        val e = MotivationInsights.energy(listOf(habit("a")), listOf(EnergyCheckin(today.toString(), EnergyLevel.Charged)), today)
        assertNull(e.highPercent)
        assertNull(e.liftPercent)
    }

    @Test fun stackProgress_countsLeadingDoneStepsAndSkipsDeletedHabits() {
        val stack = HabitStack("s", "Morning", "", listOf(
            StackStep("coffee", 0, null), StackStep("gone", 1, null), StackStep("journal", 2, 5), StackStep("stretch", 3, 10),
        ))
        val habits = listOf(habit("coffee", listOf(today)), habit("journal"), habit("stretch", listOf(today)))
        val p = MotivationInsights.stackProgress(stack, habits, today)
        assertEquals(3, p.steps.size)      // "gone" dropped
        assertEquals(1, p.done)            // stretch done out of order doesn't count
        assertEquals("journal", p.next!!.second.id)
        assertFalse(p.complete)
        val all = MotivationInsights.stackProgress(stack, habits.map { it.copy(completedDates = listOf(today.toString())) }, today)
        assertTrue(all.complete)
    }
}
