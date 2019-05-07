package io.plastique.deviations.viewer

import com.sch.neon.Event
import io.plastique.core.session.Session

sealed class DeviationViewerEvent : Event() {
    data class DeviationLoadedEvent(val result: DeviationLoadResult) : DeviationViewerEvent()

    data class LoadErrorEvent(val error: Throwable) : DeviationViewerEvent()
    object RetryClickEvent : DeviationViewerEvent()

    object DownloadOriginalClickEvent : DeviationViewerEvent()
    data class DownloadOriginalErrorEvent(val error: Throwable) : DeviationViewerEvent()

    data class SetFavoriteEvent(val deviationId: String, val favorite: Boolean) : DeviationViewerEvent()
    object SetFavoriteFinishedEvent : DeviationViewerEvent()
    data class SetFavoriteErrorEvent(val error: Throwable) : DeviationViewerEvent()

    object SnackbarShownEvent : DeviationViewerEvent()

    data class SessionChangedEvent(val session: Session) : DeviationViewerEvent()
}
