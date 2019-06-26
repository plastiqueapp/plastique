package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.users.UserEntity
import org.threeten.bp.Instant

@Entity(
    tableName = "deviations",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["author_id"])
    ],
    indices = [
        Index("author_id")
    ])
data class DeviationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "category_path")
    val categoryPath: String,

    @ColumnInfo(name = "publish_time")
    val publishTime: Instant,

    @ColumnInfo(name = "excerpt")
    val excerpt: String? = null,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @Embedded(prefix = "properties_")
    val properties: Properties,

    @Embedded(prefix = "stats_")
    val stats: Stats
) {
    data class Properties(
        @ColumnInfo(name = "is_downloadable")
        val isDownloadable: Boolean,

        @ColumnInfo(name = "is_favorite")
        val isFavorite: Boolean,

        @ColumnInfo(name = "is_mature")
        val isMature: Boolean,

        @ColumnInfo(name = "allows_comments")
        val allowsComments: Boolean,

        @ColumnInfo(name = "download_size")
        val downloadFileSize: Long
    )

    data class Stats(
        @ColumnInfo(name = "comments")
        val comments: Int,

        @ColumnInfo(name = "favorites")
        val favorites: Int
    )
}
