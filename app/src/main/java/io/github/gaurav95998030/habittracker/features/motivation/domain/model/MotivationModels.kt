package io.github.gaurav95998030.habittracker.features.motivation.domain.model

/** Reasons offered when logging a miss. [icon] is a Phosphor name. */
enum class MissReason(val label: String, val icon: String) {
    Sick("Sick", "ph-thermometer"),
    Travel("Travel", "ph-airplane-tilt"),
    Busy("Busy", "ph-briefcase"),
    RestDay("Rest day", "ph-couch"),
    Forgot("Forgot", "ph-question");

    companion object {
        fun fromStorage(value: String): MissReason? = entries.firstOrNull { it.name == value }
    }
}

/** A logged miss. [date] is "yyyy-MM-dd". */
data class MissLog(val habitId: String, val date: String, val reason: MissReason)

/** Energy levels, 1..5. [icon] is a Phosphor name; [colorHex] an accent. */
enum class EnergyLevel(val level: Int, val label: String, val icon: String, val colorHex: String) {
    Drained(1, "Drained", "ph-battery-empty", "#FF6F61"),
    Low(2, "Low", "ph-battery-low", "#FFB23E"),
    Okay(3, "Okay", "ph-battery-medium", "#29D4D9"),
    Good(4, "Good", "ph-battery-high", "#34E5A0"),
    Charged(5, "Charged", "ph-lightning", "#B6FF3C");

    companion object {
        fun of(level: Int): EnergyLevel? = entries.firstOrNull { it.level == level }
    }
}

data class EnergyCheckin(val date: String, val level: EnergyLevel)

data class StackStep(val habitId: String, val position: Int, val minutes: Int?)

/** A named, ordered chain of habits. [steps] are sorted by position. */
data class HabitStack(val id: String, val name: String, val createdDate: String, val steps: List<StackStep>)
