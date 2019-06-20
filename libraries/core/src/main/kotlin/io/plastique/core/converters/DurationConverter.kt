package io.plastique.core.converters

import androidx.room.TypeConverter
import org.threeten.bp.Duration

class DurationConverter {
    @TypeConverter
    fun toLong(duration: Duration): Long {
        return duration.seconds
    }

    @TypeConverter
    fun fromLong(seconds: Long): Duration {
        return Duration.ofSeconds(seconds)
    }
}
