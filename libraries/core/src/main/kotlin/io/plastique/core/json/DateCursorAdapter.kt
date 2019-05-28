package io.plastique.core.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.plastique.core.paging.DateCursor
import org.threeten.bp.LocalDate

class DateCursorAdapter {
    @ToJson
    fun toJson(cursor: DateCursor): String {
        return cursor.date.toString()
    }

    @FromJson
    fun fromJson(json: String): DateCursor {
        return DateCursor(LocalDate.parse(json))
    }
}
