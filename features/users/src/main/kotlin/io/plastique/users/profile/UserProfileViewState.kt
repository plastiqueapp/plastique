package io.plastique.users.profile

import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.snackbar.SnackbarState

data class UserProfileViewState(
    val contentState: ContentState,
    val username: String,
    val currentUserId: String?,
    val title: String = "",
    val userProfile: UserProfile? = null,
    val showProgressDialog: Boolean = false,
    val snackbarState: SnackbarState? = null,
    val emptyState: EmptyState? = null
) {
    val isSignedIn: Boolean
        get() = currentUserId != null

    val isCurrentUser: Boolean
        get() = currentUserId == userProfile?.user?.id
}
