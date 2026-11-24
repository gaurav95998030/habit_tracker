package io.github.gaurav95998030.habittracker.features.motivation.domain

data class Quote(val text: String, val author: String)

/** Daily-boost quotes; two rotate each day by day-of-year. */
object Quotes {
    val all: List<Quote> = listOf(
        Quote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Will Durant"),
        Quote("Small deeds done are better than great deeds planned.", "Peter Marshall"),
        Quote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
        Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
        Quote("Don’t watch the clock; do what it does. Keep going.", "Sam Levenson"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
    )

    /** Saved quotes are stored as "text — author". */
    fun storageKey(q: Quote) = "${q.text} — ${q.author}"
}
