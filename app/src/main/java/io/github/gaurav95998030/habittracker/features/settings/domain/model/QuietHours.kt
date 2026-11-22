package io.github.gaurav95998030.habittracker.features.settings.domain.model

/**
 * A nightly do-not-disturb window for reminders. [start]/[end] are "HH:mm".
 * The window may wrap midnight (22:00 → 07:00), which is the common case.
 */
data class QuietHours(
    val enabled: Boolean = false,
    val start: String = DEFAULT_START,
    val end: String = DEFAULT_END,
) {
    /** True when [minuteOfDay] (0..1439) falls inside the window. */
    fun contains(minuteOfDay: Int): Boolean {
        if (!enabled) return false
        val s = toMinutes(start) ?: return false
        val e = toMinutes(end) ?: return false
        return when {
            s == e -> false
            s < e -> minuteOfDay in s until e
            else -> minuteOfDay >= s || minuteOfDay < e
        }
    }

    companion object {
        const val DEFAULT_START = "22:00"
        const val DEFAULT_END = "07:00"

        fun toMinutes(hhmm: String): Int? {
            val parts = hhmm.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return h * 60 + m
        }
    }
}
