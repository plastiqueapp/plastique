package io.plastique.users

import io.plastique.core.content.ContentState

data class UserProfileViewState(
    val contentState: ContentState,
    val username: String,
    val title: String = "",
    val userProfile: UserProfile? = null,
    val showLinkCopiedMessage: Boolean = false
)
