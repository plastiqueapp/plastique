package io.plastique.watch

import com.sch.neon.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.network.NetworkConnectionState

sealed class WatcherListEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : WatcherListEvent() {
        override fun toString(): String = "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : WatcherListEvent()

    object LoadMoreEvent : WatcherListEvent()
    object LoadMoreStartedEvent : WatcherListEvent()
    object LoadMoreFinishedEvent : WatcherListEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : WatcherListEvent()

    object RefreshEvent : WatcherListEvent()
    object RefreshFinishedEvent : WatcherListEvent()
    data class RefreshErrorEvent(val error: Throwable) : WatcherListEvent()

    object RetryClickEvent : WatcherListEvent()
    object SnackbarShownEvent : WatcherListEvent()

    data class ConnectionStateChangedEvent(val connectionState: NetworkConnectionState) : WatcherListEvent()
    data class UserChangedEvent(val userId: String?) : WatcherListEvent()
}
