package io.github.gaurav95998030.habittracker.features.habits.data.local

import androidx.room.TypeConverter

class ListConverter {

    @TypeConverter
    fun fromList(list: List<String>): String =
        list.joinToString(",")

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(",")
}
