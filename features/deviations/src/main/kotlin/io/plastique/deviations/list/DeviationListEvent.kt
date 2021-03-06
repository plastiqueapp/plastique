package io.plastique.deviations.list

import com.sch.neon.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.network.NetworkConnectionState
import io.plastique.deviations.FetchParams

sealed class DeviationListEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : DeviationListEvent() {
        override fun toString(): String =
            "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : DeviationListEvent()
    object RetryClickEvent : DeviationListEvent()
    object SnackbarShownEvent : DeviationListEvent()

    object LoadMoreEvent : DeviationListEvent()
    object LoadMoreStartedEvent : DeviationListEvent()
    object LoadMoreFinishedEvent : DeviationListEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : DeviationListEvent()

    object RefreshEvent : DeviationListEvent()
    object RefreshFinishedEvent : DeviationListEvent()
    data class RefreshErrorEvent(val error: Throwable) : DeviationListEvent()

    data class SetFavoriteEvent(val deviationId: String, val favorite: Boolean) : DeviationListEvent()
    object SetFavoriteFinishedEvent : DeviationListEvent()
    data class SetFavoriteErrorEvent(val error: Throwable) : DeviationListEvent()

    data class ConnectionStateChangedEvent(val connectionState: NetworkConnectionState) : DeviationListEvent()
    data class ParamsChangedEvent(val params: FetchParams) : DeviationListEvent()
    data class ShowLiteratureChangedEvent(val showLiterature: Boolean) : DeviationListEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : DeviationListEvent()
    data class LayoutModeChangedEvent(val layoutMode: LayoutMode) : DeviationListEvent()
}
