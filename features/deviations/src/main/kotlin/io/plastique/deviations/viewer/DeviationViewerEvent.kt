package io.plastique.deviations.viewer

import com.sch.neon.Event

sealed class DeviationViewerEvent : Event() {
    data class DeviationLoadedEvent(val result: DeviationLoadResult) : DeviationViewerEvent()

    data class LoadErrorEvent(val error: Throwable) : DeviationViewerEvent()
    object RetryClickEvent : DeviationViewerEvent()

    object CopyLinkClickEvent : DeviationViewerEvent()
    object DownloadOriginalClickEvent : DeviationViewerEvent()
    data class DownloadOriginalErrorEvent(val error: Throwable) : DeviationViewerEvent()

    data class SetFavoriteEvent(val favorite: Boolean) : DeviationViewerEvent()
    object SetFavoriteFinishedEvent : DeviationViewerEvent()
    data class SetFavoriteErrorEvent(val error: Throwable) : DeviationViewerEvent()

    object SnackbarShownEvent : DeviationViewerEvent()

    data class UserChangedEvent(val userId: String?) : DeviationViewerEvent()
}
