package io.github.gaurav95998030.habittracker.features.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.util.UUID
import kotlin.math.roundToInt

data class DayStatus(
    val date: LocalDate,
    val isCompleted: Boolean
)


object HabitUtil{
    fun getDatesOfCurrentYear(): List<LocalDate> {
        val year = Year.now().value
        val start = LocalDate.of(year, 1, 1)
        val daysInYear = Year.of(year).length() // 365 or 366

        return (0 until daysInYear).map { start.plusDays(it.toLong()) }
    }
    fun getDatesOfCurrentMonth(): List<LocalDate> {
        val now = LocalDate.now()
        val yearMonth = YearMonth.of(now.year, now.month)
        val start = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()

        return (0 until daysInMonth).map {
            start.plusDays(it.toLong())
        }
    }

    fun getLast7Days(completedDates: Set<LocalDate>): List<DayStatus> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            DayStatus(
                date = date,
                isCompleted = completedDates.contains(date)
            )
        }
    }


    fun generateUniqueId(): String = UUID.randomUUID().toString()

    /** "21:30" → "9:30 PM". Returns the input unchanged when it isn't "HH:mm". */
    fun formatTime12h(hhmm: String?): String {
        if (hhmm == null) return ""
        val parts = hhmm.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return hhmm
        val h12 = if (h % 12 == 0) 12 else h % 12
        return "%d:%02d %s".format(h12, m, if (h < 12) "AM" else "PM")
    }

    /**
     * The live streak: consecutive completed days ending today, or ending yesterday
     * when today isn't checked off yet (so a streak stays "alive" until the day passes).
     * Returns 0 when neither today nor yesterday is completed.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun currentStreak(dates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        if (dates.isEmpty()) return 0
        var anchor = when {
            dates.contains(today) -> today
            dates.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }
        var count = 0
        while (dates.contains(anchor)) {
            count++
            anchor = anchor.minusDays(1)
        }
        return count
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun maxStreak(dates: Set<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var best = 0
        var current = 0
        var prev: LocalDate? = null
        dates.sorted().forEach { d ->
            current = if (prev?.plusDays(1) == d) current + 1 else 1
            if (current > best) best = current
            prev = d
        }
        return best
    }


    fun colorToHex(color: Color, includeAlpha: Boolean = true): String {
        val a = (color.alpha * 255).roundToInt()
        val r = (color.red   * 255).roundToInt()
        val g = (color.green * 255).roundToInt()
        val b = (color.blue  * 255).roundToInt()

        return if (includeAlpha) {
            String.format("#%02X%02X%02X%02X", a, r, g, b) // AARRGGBB
        } else {
            String.format("#%02X%02X%02X", r, g, b)        // RRGGBB
        }
    }

    fun hexToColor(hex: String): Color {
        val cleaned = hex
            .removePrefix("#")
            .removePrefix("0x")
            .uppercase()

        return when (cleaned.length) {
            8 -> { // AARRGGBB
                val a = cleaned.take(2).toInt(16)
                val r = cleaned.substring(2, 4).toInt(16)
                val g = cleaned.substring(4, 6).toInt(16)
                val b = cleaned.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            6 -> { // RRGGBB (assume alpha = FF)
                val r = cleaned.take(2).toInt(16)
                val g = cleaned.substring(2, 4).toInt(16)
                val b = cleaned.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            else -> throw IllegalArgumentException("Invalid hex color: $hex")
        }
    }

}