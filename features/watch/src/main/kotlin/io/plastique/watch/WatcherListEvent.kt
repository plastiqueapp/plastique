package io.plastique.watch

import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session
import io.plastique.util.NetworkConnectionState

sealed class WatcherListEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : WatcherListEvent() {
        override fun toString(): String = "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : WatcherListEvent()

    object LoadMoreEvent : WatcherListEvent()
    object LoadMoreFinishedEvent : WatcherListEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : WatcherListEvent()

    object RefreshEvent : WatcherListEvent()
    object RefreshFinishedEvent : WatcherListEvent()
    data class RefreshErrorEvent(val error: Throwable) : WatcherListEvent()

    object RetryClickEvent : WatcherListEvent()
    object SnackbarShownEvent : WatcherListEvent()

    data class ConnectionStateChangedEvent(val connectionState: NetworkConnectionState) : WatcherListEvent()
    data class SessionChangedEvent(val session: Session) : WatcherListEvent()
}
