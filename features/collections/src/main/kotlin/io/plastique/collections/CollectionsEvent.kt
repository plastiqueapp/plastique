package io.plastique.collections

import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session

sealed class CollectionsEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : CollectionsEvent() {
        override fun toString(): String =
                "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val errorState: EmptyState) : CollectionsEvent()

    object LoadMoreEvent : CollectionsEvent()
    object LoadMoreFinishedEvent : CollectionsEvent()
    data class LoadMoreErrorEvent(val errorMessage: String) : CollectionsEvent()

    object RefreshEvent : CollectionsEvent()
    object RefreshFinishedEvent : CollectionsEvent()
    data class RefreshErrorEvent(val errorMessage: String) : CollectionsEvent()

    object RetryClickEvent : CollectionsEvent()
    object SnackbarShownEvent : CollectionsEvent()

    data class SessionChangedEvent(val session: Session) : CollectionsEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : CollectionsEvent()

    data class CreateFolderEvent(val folderName: String) : CollectionsEvent()
    data class DeleteFolderEvent(val folder: Folder) : CollectionsEvent()
}
