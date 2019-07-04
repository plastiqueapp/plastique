package io.plastique.notifications

import com.sch.neon.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session

sealed class NotificationsEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : NotificationsEvent() {
        override fun toString(): String =
            "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : NotificationsEvent()
    object RetryClickEvent : NotificationsEvent()

    object LoadMoreEvent : NotificationsEvent()
    object LoadMoreStartedEvent : NotificationsEvent()
    object LoadMoreFinishedEvent : NotificationsEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : NotificationsEvent()

    object RefreshEvent : NotificationsEvent()
    object RefreshFinishedEvent : NotificationsEvent()
    data class RefreshErrorEvent(val error: Throwable) : NotificationsEvent()

    object SnackbarShownEvent : NotificationsEvent()
    data class SessionChangedEvent(val session: Session) : NotificationsEvent()

    data class DeleteMessageEvent(val messageId: String) : NotificationsEvent()
    data class UndoDeleteMessageEvent(val messageId: String) : NotificationsEvent()
}
