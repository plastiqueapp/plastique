package io.plastique.comments

import io.plastique.users.UserEntity
import io.plastique.users.UserEntityMapper
import javax.inject.Inject

class CommentEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
) {
    fun map(comment: CommentEntity, author: UserEntity): Comment {
        if (comment.authorId != author.id) {
            throw IllegalArgumentException("Expected user with id ${comment.authorId} but got ${author.id}")
        }
        return Comment(
                id = comment.id,
                parentId = comment.parentId,
                author = userEntityMapper.map(author),
                datePosted = comment.datePosted,
                text = comment.text)
    }

    fun map(commentWithAuthor: CommentWithAuthor): Comment {
        return map(commentWithAuthor.comment, commentWithAuthor.authors.first())
    }
}
