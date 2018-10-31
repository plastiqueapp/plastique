package io.plastique.deviations.viewer

import io.plastique.core.content.ContentState
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.Deviation

data class DeviationViewerViewState(
    val deviationId: String,

    val contentState: ContentState,
    val signedIn: Boolean,
    val menuState: MenuState = MenuState(),
    val deviation: Deviation? = null,
    val snackbarState: SnackbarState = SnackbarState.None
) {
    data class MenuState(
        val showFavorite: Boolean = false,
        val showDownload: Boolean = false,
        val isFavoriteChecked: Boolean = false
    )
}
