package io.plastique.comments

import io.plastique.users.User
import io.plastique.users.UserEntity
import io.plastique.users.toUser
import org.threeten.bp.ZonedDateTime

data class Comment(
    val id: String,
    val parentId: String?,
    val author: User,
    val datePosted: ZonedDateTime,
    val text: String
)

fun CommentEntity.toComment(author: UserEntity): Comment {
    if (authorId != author.id) {
        throw IllegalArgumentException("Expected user with id $authorId but got ${author.id}")
    }
    return Comment(
            id = id,
            parentId = parentId,
            author = author.toUser(),
            datePosted = datePosted,
            text = text)
}

fun CommentWithAuthor.toComment(): Comment = comment.toComment(authors.first())
