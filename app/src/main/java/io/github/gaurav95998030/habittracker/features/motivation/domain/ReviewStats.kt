package io.github.gaurav95998030.habittracker.features.motivation.domain

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.IsoFields
import kotlin.math.roundToInt

/** One day across all active habits. */
data class DayTally(val date: LocalDate, val done: Int, val total: Int) {
    val percent: Int get() = if (total == 0) 0 else (done * 100f / total).roundToInt()
}

data class HabitCount(val habit: Habit, val count: Int, val streak: Int)

data class WeekReview(
    val days: List<DayTally>,
    val percent: Int,
    val deltaVsPrevious: Int,
    val best: DayTally,
    val perfectDays: Int,
    /** Sorted by days done (desc), then current streak. First = MVP. */
    val ranking: List<HabitCount>,
    val checkIns: Int,
    val habitCount: Int,
)

data class HabitMonth(val habit: Habit, val count: Int, val ratePercent: Int, val bestRun: Int)

data class MonthReview(
    val month: YearMonth,
    val days: List<DayTally>,
    val total: Int,
    val percent: Int,
    val deltaVsPrevious: Int,
    val previous: YearMonth,
    /** Completion % per 7-day chunk starting on the 1st. */
    val weeks: List<Int>,
    val bestWeek: Int,
    val longest: HabitMonth?,
    val mostConsistent: List<HabitMonth>,
    val habitCount: Int,
)

data class YearReview(
    val year: Int,
    val daysShowedUp: Int,
    val checkIns: Int,
    val habitCount: Int,
    val longestStreak: Int,
    val longestHabit: Habit?,
    val longestFrom: LocalDate?,
    val longestTo: LocalDate?,
    /** Completion % per month (0 for future months). */
    val months: List<Int>,
    val bestMonth: Int,
    /** Last 26 weeks of daily completion %, oldest first (for the share card heat strip). */
    val recentDays: List<Int>,
)

data class BoostContent(
    val quotes: List<Quote>,
    val hero: HabitCount?,
    val activeCount: Int,
)

/** A broken 5+ day streak with recent consistency to show instead of a zero. */
data class Comeback(
    val brokenRun: Int,
    /** Oldest → today. */
    val last10: List<Boolean>,
    val bestRunThisMonth: Int,
    val consistency30: Int,
) {
    val hitsLast10: Int get() = last10.count { it }
}

/**
 * Pure stats behind the stories and reviews (the design's reviews.jsx / features.jsx).
 * Only active habits count. Everything takes `today` so it's testable.
 */
@RequiresApi(Build.VERSION_CODES.O)
object ReviewStats {

    private fun tally(active: List<Habit>, sets: Map<String, Set<LocalDate>>, d: LocalDate) =
        DayTally(d, active.count { d in sets[it.id].orEmpty() }, active.size)

    private fun pct(done: Int, slots: Int) = if (slots == 0) 0 else (done * 100f / slots).roundToInt()

    private fun setsOf(active: List<Habit>) = active.associate { it.id to HabitStats.completedSet(it) }

    fun week(habits: List<Habit>, today: LocalDate = LocalDate.now()): WeekReview {
        val active = habits.filter { it.isActive }
        val sets = setsOf(active)
        val n = active.size.coerceAtLeast(1)
        val days = (6 downTo 0).map { tally(active, sets, today.minusDays(it.toLong())) }
        val prev = (13 downTo 7).map { tally(active, sets, today.minusDays(it.toLong())) }
        val percent = pct(days.sumOf { it.done }, 7 * n)
        val ranking = active.map { h ->
            val set = sets[h.id].orEmpty()
            HabitCount(h, days.count { it.date in set }, HabitUtil.currentStreak(set, today))
        }.sortedWith(compareByDescending<HabitCount> { it.count }.thenByDescending { it.streak })
        return WeekReview(
            days = days,
            percent = percent,
            deltaVsPrevious = percent - pct(prev.sumOf { it.done }, 7 * n),
            best = days.reduce { a, b -> if (b.percent > a.percent) b else a },
            perfectDays = days.count { it.total > 0 && it.done == it.total },
            ranking = ranking,
            checkIns = days.sumOf { it.done },
            habitCount = active.size,
        )
    }

    /** Review of the month [back] months before today's (1 = last month). */
    fun month(habits: List<Habit>, today: LocalDate = LocalDate.now(), back: Long = 1): MonthReview {
        val active = habits.filter { it.isActive }
        val sets = setsOf(active)
        val n = active.size.coerceAtLeast(1)
        val ym = YearMonth.from(today).minusMonths(back)
        val pm = ym.minusMonths(1)
        fun daysOf(m: YearMonth) = (1..m.lengthOfMonth()).map { tally(active, sets, m.atDay(it)) }
        val days = daysOf(ym)
        val total = days.sumOf { it.done }
        val percent = pct(total, days.size * n)
        val weeks = days.chunked(7).map { ch -> pct(ch.sumOf { it.done }, ch.size * n) }
        val per = active.map { h ->
            val set = sets[h.id].orEmpty()
            var count = 0; var run = 0; var best = 0
            days.forEach { if (it.date in set) { count++; run++; best = maxOf(best, run) } else run = 0 }
            HabitMonth(h, count, pct(count, days.size), best)
        }
        return MonthReview(
            month = ym,
            days = days,
            total = total,
            percent = percent,
            deltaVsPrevious = percent - pct(daysOf(pm).sumOf { it.done }, pm.lengthOfMonth() * n),
            previous = pm,
            weeks = weeks,
            bestWeek = weeks.indices.maxByOrNull { weeks[it] } ?: 0,
            longest = per.maxByOrNull { it.bestRun },
            mostConsistent = per.sortedByDescending { it.ratePercent },
            habitCount = active.size,
        )
    }

    fun year(habits: List<Habit>, year: Int, today: LocalDate = LocalDate.now()): YearReview {
        val active = habits.filter { it.isActive }
        val sets = setsOf(active)
        val n = active.size.coerceAtLeast(1)
        val inYear = sets.mapValues { (_, s) -> s.filterTo(HashSet()) { it.year == year && !it.isAfter(today) } }

        var longest = 0; var longestHabit: Habit? = null; var from: LocalDate? = null; var to: LocalDate? = null
        active.forEach { h ->
            var run = 0; var start: LocalDate? = null
            inYear[h.id].orEmpty().sorted().forEach { d ->
                if (start != null && d == start!!.plusDays(run.toLong())) run++ else { start = d; run = 1 }
                if (run > longest) { longest = run; longestHabit = h; from = start; to = d }
            }
        }
        val months = (1..12).map { m ->
            val ym = YearMonth.of(year, m)
            val last = minOf(ym.lengthOfMonth(), if (ym == YearMonth.from(today)) today.dayOfMonth else ym.lengthOfMonth())
            if (ym.isAfter(YearMonth.from(today))) 0
            else pct((1..last).sumOf { tally(active, sets, ym.atDay(it)).done }, last * n)
        }
        val end = minOf(today, LocalDate.of(year, 12, 31))
        return YearReview(
            year = year,
            daysShowedUp = inYear.values.flatten().toSet().size,
            checkIns = inYear.values.sumOf { it.size },
            habitCount = active.size,
            longestStreak = longest,
            longestHabit = longestHabit,
            longestFrom = from,
            longestTo = to,
            months = months,
            bestMonth = months.indices.maxByOrNull { months[it] } ?: 0,
            recentDays = (26 * 7 - 1 downTo 0).map { tally(active, sets, end.minusDays(it.toLong())).percent },
        )
    }

    fun boost(habits: List<Habit>, today: LocalDate = LocalDate.now()): BoostContent {
        val active = habits.filter { it.isActive }
        val doy = today.dayOfYear - 1
        val hero = active.map { HabitCount(it, 0, HabitUtil.currentStreak(HabitStats.completedSet(it), today)) }
            .maxByOrNull { it.streak }
        return BoostContent(
            quotes = listOf(Quotes.all[doy % Quotes.all.size], Quotes.all[(doy + 3) % Quotes.all.size]),
            hero = hero,
            activeCount = active.size,
        )
    }

    /**
     * Comeback mode: shown when the most recent run *before* the current streak was 5+
     * days, ended within the last week, and the current streak hasn't caught up yet.
     */
    fun comeback(habit: Habit, today: LocalDate = LocalDate.now()): Comeback? {
        if (!habit.isActive) return null
        val done = HabitStats.completedSet(habit)
        val cur = HabitUtil.currentStreak(done, today)
        val searchFrom = if (cur > 0) {
            val anchor = if (today in done) today else today.minusDays(1)
            anchor.minusDays(cur.toLong() + 1) // the day before the current run's start is a miss
        } else today.minusDays(2)
        val window = today.minusDays(7)
        var end = searchFrom
        while (!end.isBefore(window) && end !in done) end = end.minusDays(1)
        if (end.isBefore(window)) return null
        var run = 0
        var d = end
        while (d in done) { run++; d = d.minusDays(1) }
        if (run < 5 || cur >= run) return null

        val created = HabitStats.createdOn(habit)
        val span = created?.let { (java.time.temporal.ChronoUnit.DAYS.between(it, today) + 1).toInt() } ?: 30
        val since30 = (0 until 30).count { today.minusDays(it.toLong()) in done }
        var bestMonth = 0; var r = 0
        (1..today.dayOfMonth).forEach { day ->
            if (today.withDayOfMonth(day) in done) { r++; bestMonth = maxOf(bestMonth, r) } else r = 0
        }
        return Comeback(
            brokenRun = run,
            last10 = (9 downTo 0).map { today.minusDays(it.toLong()) in done },
            bestRunThisMonth = bestMonth,
            consistency30 = pct(since30, span.coerceIn(1, 30)),
        )
    }

    // ── Story identity (for the seen/unseen ring) ──

    fun boostKey(today: LocalDate) = "boost:$today"
    fun weekKey(today: LocalDate) = "week:${today.get(IsoFields.WEEK_BASED_YEAR)}-W${today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
    fun monthKey(today: LocalDate) = "month:${YearMonth.from(today).minusMonths(1)}"
    fun yearKey(year: Int) = "year:$year"

    /** "Wrapped" is offered in December (this year) and January (last year). */
    fun wrappedYear(today: LocalDate): Int? = when (today.monthValue) {
        12 -> today.year
        1 -> today.year - 1
        else -> null
    }
}
