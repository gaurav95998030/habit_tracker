package io.github.gaurav95998030.habittracker.features.habits

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit

/** A focus area. [icon] is a Phosphor name ("ph-xxx"). */
data class HabitCategory(val name: String, val icon: String)

/** A one-tap starter habit offered during onboarding. [colorHex] is #RRGGBB. */
data class StarterHabit(val name: String, val icon: String, val colorHex: String, val category: String)

/**
 * Static catalog behind the add-habit form and onboarding: the 8 design categories,
 * the starter habits, and the icon fallback for habits that predate per-habit icons
 * (or use one of the older free-form category names).
 */
object HabitCatalog {

    val categories: List<HabitCategory> = listOf(
        HabitCategory("Mind", "ph-brain"),
        HabitCategory("Body", "ph-barbell"),
        HabitCategory("Focus", "ph-target"),
        HabitCategory("Health", "ph-heartbeat"),
        HabitCategory("Creative", "ph-palette"),
        HabitCategory("Social", "ph-users-three"),
        HabitCategory("Money", "ph-piggy-bank"),
        HabitCategory("Sleep", "ph-moon-stars"),
    )

    const val DEFAULT_CATEGORY = "Mind"

    val starters: List<StarterHabit> = listOf(
        StarterHabit("Morning run", "ph-sneaker-move", "#B6FF3C", "Body"),
        StarterHabit("Meditate", "ph-flower-lotus", "#34E5A0", "Mind"),
        StarterHabit("Read 20 pages", "ph-book-open", "#3BA9FF", "Mind"),
        StarterHabit("Drink water", "ph-drop", "#29D4D9", "Health"),
        StarterHabit("Journal", "ph-notebook", "#FF6AD5", "Mind"),
        StarterHabit("Stretch", "ph-person-simple-walk", "#FFB23E", "Body"),
    )

    /** Quick reminder times offered in the add sheet ("HH:mm"). */
    val quickTimes: List<String> = listOf(
        "06:30", "07:00", "07:30", "08:00", "12:00", "18:00", "20:00", "21:30", "22:45",
    )

    const val DEFAULT_REMINDER_TIME = "07:30"

    /** Keyword → icon for legacy free-form category names. First match wins. */
    private val legacyKeywords: List<Pair<String, String>> = listOf(
        "medit" to "ph-flower-lotus", "mind" to "ph-brain", "mental" to "ph-brain",
        "yoga" to "ph-person-simple-walk", "walk" to "ph-sneaker-move", "step" to "ph-sneaker-move",
        "run" to "ph-sneaker-move", "fit" to "ph-barbell", "workout" to "ph-barbell", "body" to "ph-barbell",
        "hydra" to "ph-drop", "water" to "ph-drop", "diet" to "ph-bowl-food", "food" to "ph-bowl-food",
        "health" to "ph-heartbeat", "self" to "ph-heart", "gratitude" to "ph-heart",
        "sleep" to "ph-moon-stars", "night" to "ph-moon-stars", "morning" to "ph-sun",
        "read" to "ph-book-open", "journal" to "ph-notebook", "study" to "ph-graduation-cap",
        "learn" to "ph-graduation-cap", "skill" to "ph-graduation-cap",
        "work" to "ph-briefcase", "career" to "ph-briefcase", "time" to "ph-timer",
        "product" to "ph-timer", "focus" to "ph-target",
        "financ" to "ph-piggy-bank", "budget" to "ph-piggy-bank", "saving" to "ph-piggy-bank", "money" to "ph-piggy-bank",
        "social" to "ph-users-three", "creat" to "ph-palette", "art" to "ph-palette",
        "routine" to "ph-calendar-blank", "growth" to "ph-sparkle",
    )

    fun categoryIcon(category: String): String {
        categories.firstOrNull { it.name.equals(category, ignoreCase = true) }?.let { return it.icon }
        val lower = category.lowercase()
        return legacyKeywords.firstOrNull { lower.contains(it.first) }?.second ?: "ph-sparkle"
    }

    /** The icon to render for [habit]: its own icon if set, else its category's. */
    fun iconFor(habit: Habit): String = habit.icon?.takeIf { it.isNotBlank() } ?: categoryIcon(habit.category)
}
