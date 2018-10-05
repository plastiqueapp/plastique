package io.plastique.comments.list

import io.plastique.comments.Comment
import io.plastique.util.HtmlCompat
import javax.inject.Inject

class CommentMapper @Inject constructor() {
    fun map(comment: Comment, parent: Comment?): CommentUiModel {
        if (comment.parentId != parent?.id) {
            throw IllegalArgumentException("Expected parent comment with id ${comment.parentId} but got ${parent?.id}")
        }
        return CommentUiModel(
                id = comment.id,
                datePosted = comment.datePosted,
                text = HtmlCompat.fromHtml(comment.text),
                author = comment.author,
                parentId = comment.parentId,
                parentAuthorName = parent?.author?.name)
    }
}
