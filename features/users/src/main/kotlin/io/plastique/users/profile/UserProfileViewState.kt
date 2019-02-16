package io.plastique.users.profile

import io.plastique.core.content.ContentState
import io.plastique.core.snackbar.SnackbarState

data class UserProfileViewState(
    val contentState: ContentState,
    val username: String,
    val currentUserId: String?,
    val title: String = "",
    val userProfile: UserProfile? = null,
    val snackbarState: SnackbarState = SnackbarState.None,
    val showProgressDialog: Boolean = false
) {
    val isCurrentUser: Boolean
        get() = currentUserId == userProfile?.user?.id
}
