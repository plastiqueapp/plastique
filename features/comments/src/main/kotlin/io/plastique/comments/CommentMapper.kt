package io.plastique.comments

import io.plastique.api.comments.Comment
import javax.inject.Inject

class CommentMapper @Inject constructor() {
    fun map(comment: Comment): CommentEntity {
        return CommentEntity(
                id = comment.id,
                parentId = comment.parentId,
                authorId = comment.author.id,
                datePosted = comment.datePosted,
                numReplies = comment.numReplies,
                hidden = comment.hidden,
                text = comment.text)
    }
}
