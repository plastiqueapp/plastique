package io.plastique.deviations.info

import androidx.room.ColumnInfo
import androidx.room.Relation
import io.plastique.users.UserEntity
import org.threeten.bp.Instant

data class DeviationInfoEntity(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @ColumnInfo(name = "publish_time")
    val publishTime: Instant,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "tags")
    val tags: List<String>,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val author: UserEntity
)
