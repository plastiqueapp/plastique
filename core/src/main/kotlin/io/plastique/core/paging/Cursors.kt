package io.plastique.core.paging

import io.plastique.api.common.PagedListResult
import org.threeten.bp.LocalDate

interface Cursor

data class OffsetCursor(val offset: Int) : Cursor {
    companion object {
        val START = OffsetCursor(0)
    }
}

data class DateCursor(val date: LocalDate) : Cursor
data class StringCursor(val value: String) : Cursor

val PagedListResult<*>.nextCursor: OffsetCursor?
    get() = if (hasMore) OffsetCursor(nextOffset!!) else null
