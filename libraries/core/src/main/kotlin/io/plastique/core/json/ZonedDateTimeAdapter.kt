package io.plastique.core.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class ZonedDateTimeAdapter(private val formatter: DateTimeFormatter) {
    @FromJson
    fun fromJson(json: String): ZonedDateTime {
        return ZonedDateTime.parse(json, formatter)
    }

    @ToJson
    fun toJson(dateTime: ZonedDateTime): String {
        return dateTime.format(formatter)
    }
}
