package io.plastique.core.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.plastique.core.paging.StringCursor

class StringCursorAdapter {
    @ToJson
    fun toJson(cursor: StringCursor): String {
        return cursor.value
    }

    @FromJson
    fun fromJson(value: String): StringCursor {
        return StringCursor(value)
    }
}
