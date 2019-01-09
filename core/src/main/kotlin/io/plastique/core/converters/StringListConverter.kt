package io.plastique.core.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isNotEmpty()) value.split(",") else emptyList()
    }
}
