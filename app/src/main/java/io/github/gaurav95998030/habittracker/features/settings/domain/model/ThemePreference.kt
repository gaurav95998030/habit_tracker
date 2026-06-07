package io.github.gaurav95998030.habittracker.features.settings.domain.model

enum class ThemePreference {
    Light,
    Dark,
    System;

    companion object {
        val Default: ThemePreference = Dark

        fun fromStorageValue(raw: String?): ThemePreference =
            values().firstOrNull { it.name == raw } ?: Default
    }
}
