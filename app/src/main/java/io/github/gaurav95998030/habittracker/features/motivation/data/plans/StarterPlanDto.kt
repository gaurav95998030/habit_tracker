package io.github.gaurav95998030.habittracker.features.motivation.data.plans

import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.PlanHabit
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StarterPlan
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wire format for starter plans — the bundled JSON uses it today and a future API should
 * return the same shape: `{ "version": 1, "plans": [ … ] }`. Unknown fields are ignored,
 * so the API can add fields without breaking older app versions.
 */
@Serializable
data class StarterPlansResponse(val version: Int = 1, val plans: List<StarterPlanDto> = emptyList())

@Serializable
data class StarterPlanDto(
    val id: String,
    val name: String,
    val days: Int,
    val color: String,
    val icon: String,
    val blurb: String = "",
    val habits: List<PlanHabitDto> = emptyList(),
)

@Serializable
data class PlanHabitDto(val name: String, val icon: String, val color: String, val category: String)

/** Parsing + validation shared by every data source (pure Kotlin — unit-tested). */
object StarterPlanParser {

    val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val HEX = Regex("^#[0-9A-Fa-f]{6}$")
    private const val FALLBACK_COLOR = "#B6FF3C"
    private const val FALLBACK_ICON = "ph-sparkle"

    fun parse(raw: String): List<StarterPlan> = map(json.decodeFromString<StarterPlansResponse>(raw))

    /**
     * Drops plans without an id/name/habits and de-duplicates ids. Values the app can't
     * render (icons not in the bundled font, odd colours, unknown categories) fall back
     * to safe defaults instead of failing — important once content comes from an API.
     */
    fun map(response: StarterPlansResponse): List<StarterPlan> {
        val seen = HashSet<String>()
        return response.plans.mapNotNull { p ->
            val habits = p.habits.filter { it.name.isNotBlank() }.map { h ->
                PlanHabit(
                    name = h.name.trim(),
                    icon = icon(h.icon),
                    colorHex = color(h.color),
                    category = HabitCatalog.categories.firstOrNull { it.name.equals(h.category, true) }?.name
                        ?: HabitCatalog.DEFAULT_CATEGORY,
                )
            }
            if (p.id.isBlank() || p.name.isBlank() || habits.isEmpty() || !seen.add(p.id)) return@mapNotNull null
            StarterPlan(
                id = p.id,
                name = p.name.trim(),
                days = p.days.coerceIn(1, 365),
                colorHex = color(p.color),
                icon = icon(p.icon),
                blurb = p.blurb.trim(),
                habits = habits,
            )
        }
    }

    private fun icon(name: String) = if (PhIcons.byName(name) != null) name else FALLBACK_ICON
    private fun color(hex: String) = if (HEX.matches(hex)) hex.uppercase() else FALLBACK_COLOR
}
