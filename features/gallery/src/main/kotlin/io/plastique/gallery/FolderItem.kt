package io.plastique.gallery

import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ListItem

data class FolderItem(val folder: Folder, override var index: Int = 0) : ListItem, IndexedItem {
    override val id: String get() = folder.id
}

data class HeaderItem(
    val folderId: String,
    val title: String
) : ListItem {
    override val id: String get() = folderId
}
