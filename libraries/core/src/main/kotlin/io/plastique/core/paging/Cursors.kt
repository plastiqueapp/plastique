package io.plastique.core.paging

import org.threeten.bp.LocalDate

interface Cursor
data class OffsetCursor(val offset: Int) : Cursor
data class DateCursor(val date: LocalDate) : Cursor
data class StringCursor(val value: String) : Cursor
