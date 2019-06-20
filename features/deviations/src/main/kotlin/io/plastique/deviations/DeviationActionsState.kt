package io.plastique.deviations

data class DeviationActionsState(
    val isFavorite: Boolean,
    val favoriteCount: Int,
    val isCommentsEnabled: Boolean,
    val commentCount: Int
)
