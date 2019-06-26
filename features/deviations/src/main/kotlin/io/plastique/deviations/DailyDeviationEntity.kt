package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.users.UserEntity
import org.threeten.bp.ZonedDateTime

@Entity(
    tableName = "daily_deviations",
    foreignKeys = [
        ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["giver_id"])
    ],
    indices = [
        Index("giver_id")
    ])
data class DailyDeviationEntity(
    @PrimaryKey
    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "date")
    val date: ZonedDateTime,

    @ColumnInfo(name = "giver_id")
    val giverId: String
)
