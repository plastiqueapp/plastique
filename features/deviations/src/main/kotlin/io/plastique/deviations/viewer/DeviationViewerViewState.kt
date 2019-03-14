package io.plastique.deviations.viewer

import io.plastique.core.content.ContentState
import io.plastique.core.snackbar.SnackbarState
import io.plastique.users.User

sealed class DeviationContent {
    data class Image(
        val url: String,
        val thumbnailUrls: List<String>
    ) : DeviationContent()

    data class Literature(
        val html: String
    ) : DeviationContent()
}

data class InfoViewState(
    val title: String,
    val author: User,

    val favoriteCount: Int,
    val isFavoriteChecked: Boolean,
    val isFavoriteEnabled: Boolean,

    val commentCount: Int,
    val isCommentsEnabled: Boolean
)

data class MenuState(
    val deviationUrl: String,
    val showDownload: Boolean,
    val downloadFileSize: Long
)

data class DeviationViewerViewState(
    val deviationId: String,

    val contentState: ContentState,
    val isSignedIn: Boolean,
    val content: DeviationContent? = null,
    val infoViewState: InfoViewState? = null,
    val menuState: MenuState? = null,
    val snackbarState: SnackbarState = SnackbarState.None,
    val showProgressDialog: Boolean = false
)
