package io.github.gaurav95998030.habittracker.features.motivation.domain.repository

import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StarterPlan

/**
 * Source of starter plans. Today backed by the bundled `assets/starter_plans.json`;
 * designed so a remote API can be dropped in behind it without touching callers.
 */
interface StarterPlanRepository {
    /** Valid plans, in display order. Never throws — returns the bundled set on failure. */
    suspend fun getPlans(): List<StarterPlan>
}
