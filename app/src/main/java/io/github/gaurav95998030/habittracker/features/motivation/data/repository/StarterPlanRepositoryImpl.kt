package io.github.gaurav95998030.habittracker.features.motivation.data.repository

import io.github.gaurav95998030.habittracker.commons.CrashReporter
import io.github.gaurav95998030.habittracker.features.motivation.data.plans.StarterPlanDataSource
import io.github.gaurav95998030.habittracker.features.motivation.data.plans.StarterPlanParser
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StarterPlan
import io.github.gaurav95998030.habittracker.features.motivation.domain.repository.StarterPlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Today: reads the bundled asset once and caches it for the process.
 *
 * To move to an API later, pass a remote source as [primary] and the asset source as
 * [fallback]: the remote list is used when it loads and parses to at least one plan,
 * otherwise the bundled plans are shown. Callers (use case, ViewModel, UI) don't change.
 */
class StarterPlanRepositoryImpl(
    private val primary: StarterPlanDataSource,
    private val fallback: StarterPlanDataSource? = null,
) : StarterPlanRepository {

    private val mutex = Mutex()
    private var cache: List<StarterPlan>? = null

    override suspend fun getPlans(): List<StarterPlan> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            load(primary).ifEmpty { fallback?.let { load(it) }.orEmpty() }
        }.also { if (it.isNotEmpty()) cache = it }
    }

    private suspend fun load(source: StarterPlanDataSource): List<StarterPlan> =
        try {
            StarterPlanParser.map(source.fetch())
        } catch (e: Exception) {
            CrashReporter.record("StarterPlans", "Failed to load plans from ${source::class.simpleName}", e)
            emptyList()
        }
}
