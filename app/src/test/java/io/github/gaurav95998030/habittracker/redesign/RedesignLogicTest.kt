package io.github.gaurav95998030.habittracker.redesign

import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.insights.domain.InsightsCalculator
import io.github.gaurav95998030.habittracker.features.reminders.presentation.NotificationCenterBuilder
import io.github.gaurav95998030.habittracker.features.reminders.presentation.ReminderStatus
import io.github.gaurav95998030.habittracker.features.settings.domain.model.QuietHours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Pure logic behind the HabitForge redesign (no Android framework needed). */
class RedesignLogicTest {

    private val today = LocalDate.of(2026, 11, 23) // a Monday

    private fun habit(
        id: String = "h",
        done: List<LocalDate> = emptyList(),
        createdDaysAgo: Long = 60,
        active: Boolean = true,
        reminder: String? = null,
        category: String = "Body",
        icon: String? = null,
    ) = Habit(
        id = id, userId = "", name = "Habit $id", description = "",
        completedDates = done.map { it.toString() },
        color = "#FFB6FF3C", category = category,
        createdDate = today.minusDays(createdDaysAgo).atStartOfDay(ZoneId.systemDefault()).toInstant().toString(),
        updatedDate = "", maxStreak = 0, isActive = active,
        reminderEnabled = reminder != null, reminderTime = reminder, icon = icon,
    )

    private fun days(vararg back: Long) = back.map { today.minusDays(it) }

    // ── Quiet hours ──

    @Test fun quietHours_wrapsMidnight() {
        val q = QuietHours(enabled = true, start = "22:00", end = "07:00")
        assertTrue(q.contains(23 * 60))
        assertTrue(q.contains(0))
        assertTrue(q.contains(6 * 60 + 59))
        assertFalse(q.contains(7 * 60))
        assertFalse(q.contains(12 * 60))
        assertTrue(q.contains(22 * 60))
    }

    @Test fun quietHours_sameDayWindow_andDisabled() {
        val q = QuietHours(enabled = true, start = "13:00", end = "15:00")
        assertTrue(q.contains(14 * 60))
        assertFalse(q.contains(15 * 60))
        assertFalse(q.copy(enabled = false).contains(14 * 60))
        assertFalse(q.copy(start = "bad").contains(14 * 60))
    }

    // ── Stats ──

    @Test fun heatLevels_scaleWithRunLength() {
        val run = (0L..14L).map { today.minusDays(it) }.toSet() + today.minusDays(20)
        val levels = HabitStats.heatLevels(run)
        assertEquals(1, levels[today.minusDays(20)])   // isolated day
        assertEquals(1, levels[today.minusDays(14)])   // run of 1
        assertEquals(2, levels[today.minusDays(12)])   // run of 3
        assertEquals(3, levels[today.minusDays(8)])    // run of 7
        assertEquals(4, levels[today.minusDays(1)])    // run of 14
        assertEquals(4, levels[today])                 // run of 15
    }

    @Test fun currentStreak_graceUntilEndOfDay() {
        assertEquals(3, HabitUtil.currentStreak(days(1, 2, 3).toSet(), today)) // today open
        assertEquals(4, HabitUtil.currentStreak(days(0, 1, 2, 3).toSet(), today))
        assertEquals(0, HabitUtil.currentStreak(days(2, 3).toSet(), today))
    }

    @Test fun createdOn_parsesInstantAndPlainDate() {
        assertEquals(today.minusDays(60), HabitStats.createdOn(habit()))
        assertEquals(today, HabitStats.createdOn(habit().copy(createdDate = today.toString())))
    }

    // ── Catalog ──

    @Test fun iconFallback_coversDesignAndLegacyCategories() {
        assertEquals("ph-brain", HabitCatalog.categoryIcon("Mind"))
        assertEquals("ph-flower-lotus", HabitCatalog.categoryIcon("Meditation"))
        assertEquals("ph-drop", HabitCatalog.categoryIcon("Hydration"))
        assertEquals("ph-sparkle", HabitCatalog.categoryIcon("Zzz-unknown"))
        assertEquals("ph-drop", HabitCatalog.iconFor(habit(icon = "ph-drop", category = "Body")))
        assertEquals("ph-barbell", HabitCatalog.iconFor(habit(icon = null, category = "Body")))
    }

    @Test fun formatTime12h() {
        assertEquals("12:05 AM", HabitUtil.formatTime12h("00:05"))
        assertEquals("9:30 PM", HabitUtil.formatTime12h("21:30"))
        assertEquals("12:00 PM", HabitUtil.formatTime12h("12:00"))
        assertEquals("oops", HabitUtil.formatTime12h("oops"))
    }

    // ── Insights ──

    @Test fun insights_weeklyRateCountsOnlyDaysSinceCreation() {
        // Created 6 days ago (7 possible days this week), done 7/7 → 100% for "Now",
        // and earlier weeks have no slots → 0%.
        val h = habit(done = (0L..6L).map { today.minusDays(it) }, createdDaysAgo = 6)
        val s = InsightsCalculator.build(listOf(h), today)
        assertEquals("Now", s.weekBuckets.last().label)
        assertEquals(100, s.weekBuckets.last().percent)
        assertEquals(0, s.weekBuckets.first().percent)
        assertEquals(7, s.totalCompletions)
    }

    @Test fun insights_excludesPausedAndRanksStreaks() {
        val a = habit("a", done = days(0, 1, 2))
        val b = habit("b", done = days(0, 1, 2, 3, 4))
        val paused = habit("p", done = days(0, 1, 2, 3, 4, 5, 6, 7), active = false)
        val s = InsightsCalculator.build(listOf(a, b, paused), today)
        assertEquals(listOf("b", "a"), s.longestStreaks.map { it.habitId })
        assertEquals(8, s.totalCompletions)
        assertTrue(InsightsCalculator.build(listOf(paused), today).isEmpty)
    }

    @Test fun insights_bestDayIsSundayIndexed() {
        val sunday = today.minusDays(1) // 2026-11-22 is a Sunday
        val s = InsightsCalculator.build(listOf(habit(done = listOf(sunday, sunday.minusWeeks(1)))), today)
        assertEquals(0, s.bestDayIndex)
        assertEquals(2, s.dayOfWeekCounts[0])
    }

    // ── Notification center ──

    @Test fun notificationCenter_statusesForToday() {
        val now = today.atTime(12, 0)
        val donePast = habit("done", done = days(0, 1), reminder = "08:00")
        val duePast = habit("due", done = days(1, 2), reminder = "09:00")
        val later = habit("later", reminder = "20:00")
        val noReminder = habit("none")
        val ui = NotificationCenterBuilder.build(listOf(donePast, duePast, later, noReminder), { "ph-x" }, now)

        val todayItems = ui.sections.first().items.associateBy { it.habitId }
        assertEquals("Today", ui.sections.first().label)
        assertEquals(ReminderStatus.Done, todayItems["done"]!!.status)
        assertEquals(ReminderStatus.Pending, todayItems["due"]!!.status)
        assertEquals(2, todayItems["due"]!!.streak) // streak through yesterday
        assertEquals(ReminderStatus.Upcoming, todayItems["later"]!!.status)
        assertFalse("none" in todayItems)
        assertEquals(1, ui.todayPending)
        assertEquals(1, ui.todayDone)
        // Latest reminder first within a day.
        assertEquals(listOf("later", "due", "done"), ui.sections.first().items.map { it.habitId })
        assertEquals(1, NotificationCenterBuilder.pendingCount(listOf(donePast, duePast, later), now))
    }

    @Test fun notificationCenter_pastDaysAndCreationCutoff() {
        val now: LocalDateTime = today.atTime(7, 0)
        val young = habit("young", reminder = "08:00", createdDaysAgo = 1, done = days(1))
        val ui = NotificationCenterBuilder.build(listOf(young), { "" }, now)
        assertEquals(listOf("Today", "Yesterday"), ui.sections.map { it.label })
        assertEquals(ReminderStatus.Done, ui.sections[1].items.single().status)
        assertEquals(ReminderStatus.Upcoming, ui.sections[0].items.single().status)
        val paused = young.copy(isActive = false)
        assertTrue(NotificationCenterBuilder.build(listOf(paused), { "" }, now).isEmpty)
    }
}
