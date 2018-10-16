package io.plastique.comments.list

import io.plastique.comments.Comment
import io.plastique.users.User
import io.plastique.util.HtmlCompat
import org.threeten.bp.ZonedDateTime

data class CommentUiModel(
    val id: String,
    val datePosted: ZonedDateTime,
    val text: CharSequence, // TODO: Need stable equals for Spanned
    val author: User,
    val parentId: String?,
    val parentAuthorName: String?
) {
    val isReply: Boolean
        get() = parentAuthorName != null
}

fun Comment.toCommentUiModel(parent: Comment?): CommentUiModel {
    if (parentId != parent?.id) {
        throw IllegalArgumentException("Expected parent comment with id $parentId but got ${parent?.id}")
    }
    return CommentUiModel(
            id = id,
            datePosted = datePosted,
            text = HtmlCompat.fromHtml(text),
            author = author,
            parentId = parentId,
            parentAuthorName = parent?.author?.name)
}
