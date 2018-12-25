package io.plastique.statuses.list

import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session

sealed class StatusListEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : StatusListEvent() {
        override fun toString(): String =
                "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : StatusListEvent()

    object LoadMoreEvent : StatusListEvent()
    object LoadMoreFinishedEvent : StatusListEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : StatusListEvent()

    object RefreshEvent : StatusListEvent()
    object RefreshFinishedEvent : StatusListEvent()
    data class RefreshErrorEvent(val error: Throwable) : StatusListEvent()

    object RetryClickEvent : StatusListEvent()
    object SnackbarShownEvent : StatusListEvent()

    data class SessionChangedEvent(val session: Session) : StatusListEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : StatusListEvent()
}
