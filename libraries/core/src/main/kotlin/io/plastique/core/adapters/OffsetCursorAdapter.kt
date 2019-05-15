package io.plastique.core.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.plastique.core.paging.OffsetCursor

class OffsetCursorAdapter {
    @ToJson
    fun toJson(cursor: OffsetCursor): Int {
        return cursor.offset
    }

    @FromJson
    fun fromJson(offset: Int): OffsetCursor {
        return OffsetCursor(offset)
    }
}
