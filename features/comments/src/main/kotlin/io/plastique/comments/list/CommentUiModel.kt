package io.plastique.comments.list

import io.plastique.users.User
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
