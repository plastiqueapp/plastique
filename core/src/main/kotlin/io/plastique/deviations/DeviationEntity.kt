package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.images.ImageEntity
import io.plastique.users.UserEntity
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "deviations",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["author_id"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["daily_deviation_giver_id"])
        ],
        indices = [
            Index("author_id"),
            Index("daily_deviation_giver_id")
        ])
data class DeviationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    @Embedded(prefix = "content_")
    val content: ImageEntity? = null,

    @Embedded(prefix = "preview_")
    val preview: ImageEntity? = null,

    @ColumnInfo(name = "excerpt")
    val excerpt: String? = null,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @Embedded(prefix = "properties_")
    val properties: DeviationPropertiesEntity,

    @Embedded(prefix = "stats_")
    val stats: DeviationStatsEntity,

    @Embedded(prefix = "daily_deviation_")
    val dailyDeviation: DailyDeviationEntity?
)

// TODO: Convert to nested once https://issuetracker.google.com/issues/119838407 is fixed
data class DailyDeviationEntity(
    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "date")
    val date: ZonedDateTime,

    @ColumnInfo(name = "giver_id")
    val giverId: String
)

data class DeviationPropertiesEntity(
    @ColumnInfo(name = "is_downloadable")
    val isDownloadable: Boolean,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "is_mature")
    val isMature: Boolean,

    @ColumnInfo(name = "allows_comments")
    val allowsComments: Boolean
)

data class DeviationStatsEntity(
    @ColumnInfo(name = "comments")
    val comments: Int,

    @ColumnInfo(name = "favorites")
    val favorites: Int
)
