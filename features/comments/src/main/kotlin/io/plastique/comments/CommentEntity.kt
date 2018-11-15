package io.plastique.comments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.api.comments.CommentDto
import io.plastique.users.UserEntity
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "comments",
        foreignKeys = [
            ForeignKey(entity = CommentEntity::class, parentColumns = ["id"], childColumns = ["parent_id"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["author_id"])
        ],
        indices = [
            Index("author_id"),
            Index("parent_id")
        ])
data class CommentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "parent_id")
    val parentId: String? = null,

    @ColumnInfo(name = "author_id")
    var authorId: String,

    @ColumnInfo(name = "date_posted")
    val datePosted: ZonedDateTime,

    @ColumnInfo(name = "replies")
    val numReplies: Int = 0,

    @ColumnInfo(name = "hidden")
    val hidden: String? = null,

    @ColumnInfo(name = "text")
    val text: String
)

fun CommentDto.toCommentEntity(): CommentEntity = CommentEntity(
        id = id,
        parentId = parentId,
        authorId = author.id,
        datePosted = datePosted,
        numReplies = numReplies,
        hidden = hidden,
        text = text)
