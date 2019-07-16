package io.plastique.core.db.converters

import androidx.room.TypeConverter
import org.threeten.bp.Instant

class InstantConverter {
    @TypeConverter
    fun toLong(value: Instant): Long {
        return value.toEpochMilli()
    }

    @TypeConverter
    fun fromLong(millis: Long): Instant {
        return Instant.ofEpochMilli(millis)
    }
}
