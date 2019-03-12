package io.plastique.deviations.viewer

import io.plastique.users.User

data class InfoViewState(
    val title: String,
    val author: User,

    val favoriteCount: Int,
    val isFavoriteChecked: Boolean,
    val isFavoriteEnabled: Boolean,

    val commentCount: Int,
    val isCommentsEnabled: Boolean
)
