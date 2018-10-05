package io.plastique.deviations

import androidx.room.ColumnInfo
import org.threeten.bp.ZonedDateTime

data class DailyDeviationEntity(
    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "date")
    val date: ZonedDateTime,

    @ColumnInfo(name = "giver_id")
    val giverId: String
)
