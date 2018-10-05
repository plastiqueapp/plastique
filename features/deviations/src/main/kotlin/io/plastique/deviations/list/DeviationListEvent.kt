package io.plastique.deviations.list

import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem
import io.plastique.deviations.FetchParams

sealed class DeviationListEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : DeviationListEvent() {
        override fun toString(): String {
            return "ItemsChangedEvent(" +
                    "items=${items.size}, " +
                    "hasMore=$hasMore, " +
                    ")"
        }
    }

    data class LoadErrorEvent(val emptyState: EmptyState) : DeviationListEvent()
    object SnackbarShown : DeviationListEvent()
    object RetryClickEvent : DeviationListEvent()

    object LoadMoreEvent : DeviationListEvent()
    object LoadMoreFinishedEvent : DeviationListEvent()
    data class LoadMoreErrorEvent(val errorMessage: String) : DeviationListEvent()

    object RefreshEvent : DeviationListEvent()
    object RefreshFinishedEvent : DeviationListEvent()
    data class RefreshErrorEvent(val errorMessage: String) : DeviationListEvent()

    data class ParamsChangedEvent(val params: FetchParams) : DeviationListEvent()
    data class ShowLiteratureChangedEvent(val showLiterature: Boolean) : DeviationListEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : DeviationListEvent()
    data class LayoutModeChangedEvent(val layoutMode: LayoutMode) : DeviationListEvent()
}
