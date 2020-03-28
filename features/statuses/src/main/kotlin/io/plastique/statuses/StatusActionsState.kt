package io.plastique.statuses

import io.plastique.comments.CommentThreadId

data class StatusActionsState internal constructor(
    val commentThreadId: CommentThreadId,
    val commentCount: Int,
    val shareObjectId: ShareObjectId?
)

fun Status.createActionsState(): StatusActionsState = StatusActionsState(
    commentThreadId = CommentThreadId.Status(id),
    commentCount = commentCount,
    shareObjectId = shareObjectId)
