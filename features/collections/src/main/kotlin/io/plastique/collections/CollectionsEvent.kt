package io.plastique.collections

import com.sch.neon.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session

sealed class CollectionsEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : CollectionsEvent() {
        override fun toString(): String =
            "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : CollectionsEvent()

    object LoadMoreEvent : CollectionsEvent()
    object LoadMoreStartedEvent : CollectionsEvent()
    object LoadMoreFinishedEvent : CollectionsEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : CollectionsEvent()

    object RefreshEvent : CollectionsEvent()
    object RefreshFinishedEvent : CollectionsEvent()
    data class RefreshErrorEvent(val error: Throwable) : CollectionsEvent()

    object RetryClickEvent : CollectionsEvent()
    object SnackbarShownEvent : CollectionsEvent()

    data class SessionChangedEvent(val session: Session) : CollectionsEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : CollectionsEvent()

    data class CreateFolderEvent(val folderName: String) : CollectionsEvent()
    object FolderCreatedEvent : CollectionsEvent()
    data class CreateFolderErrorEvent(val error: Throwable) : CollectionsEvent()

    data class DeleteFolderEvent(val folderId: String, val folderName: String) : CollectionsEvent()
    data class FolderDeletedEvent(val folderId: String, val folderName: String) : CollectionsEvent()
    data class UndoDeleteFolderEvent(val folderId: String) : CollectionsEvent()
}
