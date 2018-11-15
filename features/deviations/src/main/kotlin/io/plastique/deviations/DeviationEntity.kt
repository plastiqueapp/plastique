package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.api.deviations.DeviationDto
import io.plastique.images.ImageEntity
import io.plastique.images.toImageEntity
import io.plastique.users.UserEntity

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

    @ColumnInfo(name = "is_downloadable")
    val isDownloadable: Boolean,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "is_mature")
    val isMature: Boolean,

    @Embedded(prefix = "content_")
    val content: ImageEntity? = null,

    @Embedded(prefix = "preview_")
    val preview: ImageEntity? = null,

    @ColumnInfo(name = "excerpt")
    val excerpt: String? = null,

    @Embedded(prefix = "daily_deviation_")
    val dailyDeviation: DailyDeviationEntity? = null,

    @ColumnInfo(name = "author_id")
    val authorId: String
)

fun DeviationDto.toDeviationEntity(): DeviationEntity = DeviationEntity(
        id = id,
        title = title,
        url = url,
        authorId = author.id,
        isDownloadable = isDownloadable,
        isFavorite = isFavorite,
        isMature = isMature,
        content = content?.toImageEntity(),
        preview = preview?.toImageEntity(),
        excerpt = excerpt,
        dailyDeviation = dailyDeviation?.toDailyDeviationEntity())
