package io.plastique.deviations

import io.plastique.comments.CommentThreadId
import io.plastique.statuses.ShareObjectId

data class DeviationActionsState internal constructor(
    val deviationId: String,
    val isFavorite: Boolean,
    val favoriteCount: Int,
    val commentThreadId: CommentThreadId?,
    val commentCount: Int,
    val shareObjectId: ShareObjectId
)

fun Deviation.createActionsState(): DeviationActionsState = DeviationActionsState(
    deviationId = id,
    isFavorite = properties.isFavorite,
    favoriteCount = stats.favorites,
    commentThreadId = if (properties.allowsComments) CommentThreadId.Deviation(id) else null,
    commentCount = stats.comments,
    shareObjectId = ShareObjectId.Deviation(id))
