package io.github.gaurav95998030.habittracker.redesign

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.ReviewStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class ReviewStatsTest {

    private val today = LocalDate.of(2026, 11, 24) // Tuesday

    private fun habit(id: String, done: List<LocalDate>, active: Boolean = true, createdDaysAgo: Long = 400) = Habit(
        id = id, userId = "", name = "H$id", description = "", completedDates = done.map { it.toString() },
        color = "#FFB6FF3C", category = "Body",
        createdDate = today.minusDays(createdDaysAgo).atStartOfDay(ZoneId.systemDefault()).toInstant().toString(),
        updatedDate = "", maxStreak = 0, isActive = active,
    )

    private fun back(vararg d: Long) = d.map { today.minusDays(it) }

    @Test fun week_percentDeltaBestAndRanking() {
        val a = habit("a", back(0, 1, 2, 3, 4, 5, 6))          // 7/7 this week
        val b = habit("b", back(0, 2, 7, 8, 9, 10, 11, 12, 13)) // 2/7 this week, 7/7 last week
        val w = ReviewStats.week(listOf(a, b), today)
        assertEquals(64, w.percent)                  // 9 of 14
        assertEquals(14, w.deltaVsPrevious)          // last week: only b, 7 of 14 → 50%
        assertEquals(today.minusDays(2), w.best.date) // first 100% day, oldest→newest
        assertEquals(2, w.perfectDays)
        assertEquals("a", w.ranking.first().habit.id)
        assertEquals(9, w.checkIns)
    }

    @Test fun month_reviewsLastFullMonth() {
        val oct = YearMonth.of(2026, 10)
        val h = habit("a", (1..10).map { oct.atDay(it) } + oct.atDay(20))
        val m = ReviewStats.month(listOf(h), today)
        assertEquals(oct, m.month)
        assertEquals(11, m.total)
        assertEquals(35, m.percent) // 11 / 31
        assertEquals(10, m.longest!!.bestRun)
        assertEquals(0, m.bestWeek) // days 1–7 all done
        assertEquals(5, m.weeks.size)
    }

    @Test fun year_longestStreakAndBestMonth() {
        val run = (0L until 20L).map { LocalDate.of(2026, 3, 2).plusDays(it) }
        val y = ReviewStats.year(listOf(habit("a", run + LocalDate.of(2025, 12, 31))), 2026, today)
        assertEquals(20, y.longestStreak)
        assertEquals(LocalDate.of(2026, 3, 2), y.longestFrom)
        assertEquals(LocalDate.of(2026, 3, 21), y.longestTo)
        assertEquals(2, y.bestMonth) // March (0-based)
        assertEquals(20, y.daysShowedUp) // 2025 excluded
        assertEquals(0, y.months[11]) // December is in the future
        assertEquals(26 * 7, y.recentDays.size)
    }

    @Test fun comeback_afterBrokenFivePlusRun() {
        // 6-day run ending 3 days ago, missed 2 days ago, done yesterday & today.
        val h = habit("a", back(0, 1) + back(3, 4, 5, 6, 7, 8))
        val cb = ReviewStats.comeback(h, today)
        assertNotNull(cb)
        assertEquals(6, cb!!.brokenRun)
        assertEquals(8, cb.hitsLast10)
        assertTrue(cb.last10.last())
    }

    @Test fun comeback_hiddenWhenRunShortOldOrRecovered() {
        assertNull(ReviewStats.comeback(habit("short", back(3, 4, 5)), today))           // run < 5
        assertNull(ReviewStats.comeback(habit("old", (10L..20L).map { today.minusDays(it) }), today)) // ended > a week ago
        val recovered = habit("rec", (0L..6L).map { today.minusDays(it) } + (8L..13L).map { today.minusDays(it) })
        assertNull(ReviewStats.comeback(recovered, today))                                // current 7 ≥ broken 6
        assertNull(ReviewStats.comeback(habit("p", back(3, 4, 5, 6, 7, 8), active = false), today))
    }

    @Test fun storyKeysAndWrappedWindow() {
        assertEquals("boost:2026-11-24", ReviewStats.boostKey(today))
        assertEquals("month:2026-10", ReviewStats.monthKey(today))
        assertEquals("week:2026-W48", ReviewStats.weekKey(today))
        assertNull(ReviewStats.wrappedYear(today))
        assertEquals(2026, ReviewStats.wrappedYear(LocalDate.of(2026, 12, 5)))
        assertEquals(2026, ReviewStats.wrappedYear(LocalDate.of(2027, 1, 5)))
    }
}
