package io.plastique.comments.list

import io.plastique.core.lists.ListItem

data class CommentItem(
    val comment: CommentUiModel,
    val showReplyButton: Boolean
) : ListItem {
    override val id: String get() = comment.id
}
