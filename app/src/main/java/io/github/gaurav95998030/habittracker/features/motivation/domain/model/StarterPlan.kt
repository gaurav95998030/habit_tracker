package io.github.gaurav95998030.habittracker.features.motivation.domain.model

/** A habit a starter plan adds. [colorHex] is "#RRGGBB"; [icon] a bundled Phosphor name. */
data class PlanHabit(val name: String, val icon: String, val colorHex: String, val category: String)

/** A multi-day program that adds several habits at once. [id] is stable (running plans are stored by id). */
data class StarterPlan(
    val id: String,
    val name: String,
    val days: Int,
    val colorHex: String,
    val icon: String,
    val blurb: String,
    val habits: List<PlanHabit>,
)
