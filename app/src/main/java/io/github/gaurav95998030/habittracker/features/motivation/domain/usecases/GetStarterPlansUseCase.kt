package io.github.gaurav95998030.habittracker.features.motivation.domain.usecases

import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StarterPlan
import io.github.gaurav95998030.habittracker.features.motivation.domain.repository.StarterPlanRepository
import javax.inject.Inject

class GetStarterPlansUseCase @Inject constructor(private val repo: StarterPlanRepository) {
    suspend operator fun invoke(): List<StarterPlan> = repo.getPlans()
}
