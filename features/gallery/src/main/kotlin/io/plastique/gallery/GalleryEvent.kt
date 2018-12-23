package io.plastique.gallery

import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem
import io.plastique.core.session.Session

sealed class GalleryEvent : Event() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : GalleryEvent() {
        override fun toString(): String =
                "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : GalleryEvent()

    object LoadMoreEvent : GalleryEvent()
    object LoadMoreFinishedEvent : GalleryEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : GalleryEvent()

    object RefreshEvent : GalleryEvent()
    object RefreshFinishedEvent : GalleryEvent()
    data class RefreshErrorEvent(val error: Throwable) : GalleryEvent()

    object RetryClickEvent : GalleryEvent()
    object SnackbarShownEvent : GalleryEvent()

    data class SessionChangedEvent(val session: Session) : GalleryEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : GalleryEvent()

    data class CreateFolderEvent(val folderName: String) : GalleryEvent()
    data class DeleteFolderEvent(val folder: Folder) : GalleryEvent()
}
