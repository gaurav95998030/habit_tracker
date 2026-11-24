package io.github.gaurav95998030.habittracker.features.motivation.domain

/** A habit a starter plan adds. [colorHex] is #RRGGBB (one of the accents). */
data class PlanHabit(val name: String, val icon: String, val colorHex: String, val category: String)

data class StarterPlan(
    val id: String,
    val name: String,
    val days: Int,
    val colorHex: String,
    val icon: String,
    val blurb: String,
    val habits: List<PlanHabit>,
)

/** The design's starter programs. */
object PlanCatalog {
    val plans: List<StarterPlan> = listOf(
        StarterPlan(
            "morning", "Morning routine", 21, "#FFB23E", "ph-sun-horizon", "Win the first hour of the day.",
            listOf(
                PlanHabit("Drink water on waking", "ph-drop", "#29D4D9", "Health"),
                PlanHabit("10-minute walk", "ph-person-simple-walk", "#34E5A0", "Body"),
                PlanHabit("Journal 1 page", "ph-notebook", "#FF6AD5", "Mind"),
            ),
        ),
        StarterPlan(
            "sleep", "Better sleep", 30, "#7C8CFF", "ph-moon-stars", "A calmer wind-down, every night.",
            listOf(
                PlanHabit("No screens after 10 PM", "ph-device-mobile-slash", "#7C8CFF", "Sleep"),
                PlanHabit("Read 10 pages", "ph-book-open", "#3BA9FF", "Mind"),
                PlanHabit("Lights out by 11", "ph-moon", "#FFB23E", "Sleep"),
                PlanHabit("No caffeine after 2 PM", "ph-coffee", "#FF6F61", "Health"),
            ),
        ),
        StarterPlan(
            "focus", "Focus reset", 21, "#B6FF3C", "ph-target", "Deep work without the drift.",
            listOf(
                PlanHabit("One 50-min deep block", "ph-timer", "#B6FF3C", "Focus"),
                PlanHabit("Plan tomorrow tonight", "ph-list-checks", "#3BA9FF", "Focus"),
                PlanHabit("Meditate 5 min", "ph-flower-lotus", "#34E5A0", "Mind"),
            ),
        ),
    )

    fun byId(id: String) = plans.firstOrNull { it.id == id }
}
