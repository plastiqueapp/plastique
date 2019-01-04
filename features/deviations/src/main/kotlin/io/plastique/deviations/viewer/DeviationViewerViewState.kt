package io.plastique.deviations.viewer

import io.plastique.core.content.ContentState
import io.plastique.core.session.Session
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.Deviation

data class DeviationViewerViewState(
    val deviationId: String,

    val contentState: ContentState,
    val session: Session,
    val menuState: MenuState = MenuState(),
    val deviation: Deviation? = null,
    val snackbarState: SnackbarState = SnackbarState.None,
    val showProgressDialog: Boolean = false
) {
    data class MenuState(
        val showDownload: Boolean = false,
        val downloadFileSize: Long = 0,
        val showFavorite: Boolean = false,
        val isFavoriteChecked: Boolean = false
    )
}
