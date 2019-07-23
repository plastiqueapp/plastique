package io.plastique.gallery

import com.sch.neon.Event
import io.plastique.core.lists.ListItem

sealed class GalleryEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : GalleryEvent() {
        override fun toString(): String =
            "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : GalleryEvent()

    object LoadMoreEvent : GalleryEvent()
    object LoadMoreStartedEvent : GalleryEvent()
    object LoadMoreFinishedEvent : GalleryEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : GalleryEvent()

    object RefreshEvent : GalleryEvent()
    object RefreshFinishedEvent : GalleryEvent()
    data class RefreshErrorEvent(val error: Throwable) : GalleryEvent()

    object RetryClickEvent : GalleryEvent()
    object SnackbarShownEvent : GalleryEvent()

    data class ShowMatureChangedEvent(val showMature: Boolean) : GalleryEvent()
    data class UserChangedEvent(val userId: String?) : GalleryEvent()

    data class CreateFolderEvent(val folderName: String) : GalleryEvent()
    object FolderCreatedEvent : GalleryEvent()
    data class CreateFolderErrorEvent(val error: Throwable) : GalleryEvent()

    data class DeleteFolderEvent(val folderId: String, val folderName: String) : GalleryEvent()
    data class FolderDeletedEvent(val folderId: String, val folderName: String) : GalleryEvent()
    data class UndoDeleteFolderEvent(val folderId: String) : GalleryEvent()
}
