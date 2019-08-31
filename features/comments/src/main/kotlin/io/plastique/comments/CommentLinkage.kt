package io.plastique.comments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.core.cache.CacheEntryEntity

@Entity(
    tableName = "comment_linkage",
    primaryKeys = ["key", "comment_id"],
    foreignKeys = [
        ForeignKey(entity = CacheEntryEntity::class, parentColumns = ["key"], childColumns = ["key"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CommentEntity::class, parentColumns = ["id"], childColumns = ["comment_id"])
    ],
    indices = [
        Index("comment_id")
    ])
data class CommentLinkage(
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "comment_id")
    val commentId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
