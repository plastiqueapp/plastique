package io.plastique.feed

import com.sch.neon.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session
import io.plastique.feed.settings.FeedSettings

sealed class FeedEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : FeedEvent() {
        override fun toString(): String =
            "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : FeedEvent()

    object LoadMoreEvent : FeedEvent()
    object LoadMoreStartedEvent : FeedEvent()
    object LoadMoreFinishedEvent : FeedEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : FeedEvent()

    object RefreshEvent : FeedEvent()
    object RefreshFinishedEvent : FeedEvent()
    data class RefreshErrorEvent(val error: Throwable) : FeedEvent()

    data class SessionChangedEvent(val session: Session) : FeedEvent()
    data class ShowMatureChangedEvent(val showMatureContent: Boolean) : FeedEvent()
    object RetryClickEvent : FeedEvent()
    object SnackbarShownEvent : FeedEvent()

    data class SetFeedSettingsEvent(val settings: FeedSettings) : FeedEvent()
    object SettingsChangedEvent : FeedEvent()
    data class SettingsChangeErrorEvent(val error: Throwable) : FeedEvent()

    data class SetFavoriteEvent(val deviationId: String, val favorite: Boolean) : FeedEvent()
    object SetFavoriteFinishedEvent : FeedEvent()
    data class SetFavoriteErrorEvent(val error: Throwable) : FeedEvent()
}
