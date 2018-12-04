package io.plastique.deviations.viewer

import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event
import io.plastique.core.session.Session
import io.plastique.deviations.Deviation

sealed class DeviationViewerEvent : Event() {
    data class DeviationLoadedEvent(val deviation: Deviation) : DeviationViewerEvent()

    data class LoadErrorEvent(val emptyState: EmptyState) : DeviationViewerEvent()
    object RetryClickEvent : DeviationViewerEvent()

    object DownloadOriginalClickEvent : DeviationViewerEvent()
    data class DownloadOriginalErrorEvent(val errorMessage: String) : DeviationViewerEvent()

    data class SetFavoriteEvent(val deviationId: String, val favorite: Boolean) : DeviationViewerEvent()
    object SetFavoriteFinishedEvent : DeviationViewerEvent()
    data class SetFavoriteErrorEvent(val error: Throwable) : DeviationViewerEvent()

    object SnackbarShownEvent : DeviationViewerEvent()

    data class SessionChangedEvent(val session: Session) : DeviationViewerEvent()
}
