package io.github.gaurav95998030.habittracker.features.motivation.data.plans

import android.content.Context

/** Where raw plan JSON comes from. Add a `RemoteStarterPlanDataSource` (Retrofit/Ktor/Firestore) later. */
interface StarterPlanDataSource {
    suspend fun fetch(): StarterPlansResponse
}

/** Bundled plans shipped in `assets/starter_plans.json` — always available offline. */
class AssetStarterPlanDataSource(private val context: Context) : StarterPlanDataSource {
    override suspend fun fetch(): StarterPlansResponse {
        val raw = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        return StarterPlanParser.json.decodeFromString(raw)
    }

    companion object {
        const val ASSET = "starter_plans.json"
    }
}
