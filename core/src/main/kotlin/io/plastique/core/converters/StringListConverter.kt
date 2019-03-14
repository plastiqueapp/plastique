package io.plastique.core.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun toString(list: List<String>): String {
        list.forEach { if (it.contains(DELIMITER)) throw IllegalArgumentException("$it must not contain delimiter $DELIMITER") }
        return list.joinToString(DELIMITER)
    }

    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isNotEmpty()) value.split(DELIMITER) else emptyList()
    }

    companion object {
        private const val DELIMITER = "|"
    }
}
