package io.plastique.collections

import io.plastique.core.lists.ListItem

data class HeaderItem(
    val folderId: String,
    val title: String
) : ListItem {
    override val id: String get() = folderId
}
