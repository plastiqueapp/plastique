package io.plastique.deviations.list

import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem
import io.plastique.deviations.FetchParams
import io.plastique.util.NetworkConnectionState

sealed class DeviationListEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : DeviationListEvent() {
        override fun toString(): String =
                "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : DeviationListEvent()
    object SnackbarShown : DeviationListEvent()
    object RetryClickEvent : DeviationListEvent()

    object LoadMoreEvent : DeviationListEvent()
    object LoadMoreFinishedEvent : DeviationListEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : DeviationListEvent()

    object RefreshEvent : DeviationListEvent()
    object RefreshFinishedEvent : DeviationListEvent()
    data class RefreshErrorEvent(val error: Throwable) : DeviationListEvent()

    data class ConnectionStateChangedEvent(val connectionState: NetworkConnectionState) : DeviationListEvent()
    data class ParamsChangedEvent(val params: FetchParams) : DeviationListEvent()
    data class ShowLiteratureChangedEvent(val showLiterature: Boolean) : DeviationListEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : DeviationListEvent()
    data class LayoutModeChangedEvent(val layoutMode: LayoutMode) : DeviationListEvent()
}
