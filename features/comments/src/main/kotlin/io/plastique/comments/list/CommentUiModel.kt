package io.plastique.comments.list

import io.plastique.core.text.SpannedWrapper
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class CommentUiModel(
    val id: String,
    val datePosted: ZonedDateTime,
    val text: SpannedWrapper,
    val author: User,
    val parentId: String?,
    val parentAuthorName: String?
) {
    val isReply: Boolean
        get() = parentAuthorName != null
}
