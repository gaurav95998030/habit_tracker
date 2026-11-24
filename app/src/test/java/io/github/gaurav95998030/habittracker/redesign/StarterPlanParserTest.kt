package io.github.gaurav95998030.habittracker.redesign

import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.motivation.data.plans.PlanHabitDto
import io.github.gaurav95998030.habittracker.features.motivation.data.plans.StarterPlanDto
import io.github.gaurav95998030.habittracker.features.motivation.data.plans.StarterPlanParser
import io.github.gaurav95998030.habittracker.features.motivation.data.plans.StarterPlansResponse
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StarterPlanParserTest {

    private val raw = File("src/main/assets/starter_plans.json").readText()

    @Test fun bundledPlans_parseCompletelyWithBundledIconsAndKnownCategories() {
        val response = StarterPlanParser.json.decodeFromString<StarterPlansResponse>(raw)
        val plans = StarterPlanParser.map(response)
        assertTrue("expected 10+ plans, got ${plans.size}", plans.size >= 10)
        assertEquals("no plan may be dropped by validation", response.plans.size, plans.size)
        assertEquals(plans.size, plans.map { it.id }.toSet().size)
        // Every icon in the file must exist in the subset font (no silent sparkle fallbacks).
        (response.plans.map { it.icon } + response.plans.flatMap { p -> p.habits.map { it.icon } }).forEach {
            assertNotNull("icon $it is not bundled in PhIcons", PhIcons.byName(it))
        }
        val categories = HabitCatalog.categories.map { it.name }.toSet()
        response.plans.flatMap { it.habits }.forEach { assertTrue("unknown category ${it.category}", it.category in categories) }
    }

    @Test fun bundledPlans_keepOriginalIdsSoRunningPlansStillResolve() {
        val ids = StarterPlanParser.parse(raw).map { it.id }
        assertTrue(ids.containsAll(listOf("morning", "sleep", "focus")))
    }

    @Test fun invalidApiDataIsDroppedOrDefaultedNotThrown() {
        val plans = StarterPlanParser.map(StarterPlansResponse(plans = listOf(
            StarterPlanDto("ok", "Good", 999, "nothex", "ph-not-bundled", habits = listOf(PlanHabitDto("A", "ph-nope", "#zzzzzz", "Nonsense"))),
            StarterPlanDto("ok", "Duplicate id", 21, "#FFFFFF", "ph-sun", habits = listOf(PlanHabitDto("B", "ph-sun", "#FFFFFF", "Mind"))),
            StarterPlanDto("empty", "No habits", 21, "#FFFFFF", "ph-sun"),
            StarterPlanDto("", "No id", 21, "#FFFFFF", "ph-sun", habits = listOf(PlanHabitDto("C", "ph-sun", "#FFFFFF", "Mind"))),
        )))
        assertEquals(listOf("ok"), plans.map { it.id })
        val p = plans.single()
        assertEquals(365, p.days)
        assertEquals("ph-sparkle", p.icon)
        assertEquals("#B6FF3C", p.colorHex)
        assertEquals("ph-sparkle", p.habits.single().icon)
        assertEquals(HabitCatalog.DEFAULT_CATEGORY, p.habits.single().category)
    }

    @Test fun unknownFieldsFromFutureApiAreIgnored() {
        val plans = StarterPlanParser.parse("""{"version":2,"etag":"x","plans":[{"id":"a","name":"A","days":7,"color":"#34E5A0","icon":"ph-sun","premium":true,"habits":[{"name":"H","icon":"ph-sun","color":"#34E5A0","category":"Body","order":1}]}]}""")
        assertEquals("a", plans.single().id)
    }
}
