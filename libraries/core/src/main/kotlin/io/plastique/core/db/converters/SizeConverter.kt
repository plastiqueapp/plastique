package io.plastique.core.db.converters

import androidx.room.TypeConverter
import io.plastique.util.Size

class SizeConverter {
    @TypeConverter
    fun fromString(string: String): Size {
        val parts = string.split('x')
        val width = parts[0].toInt()
        val height = parts[1].toInt()
        return Size(width, height)
    }

    @TypeConverter
    fun toString(size: Size): String {
        return "${size.width}x${size.height}"
    }
}
