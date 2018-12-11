package io.plastique.feed.settings

import io.plastique.core.lists.ListItem

data class OptionItem(
    val key: String,
    val title: String,
    val isChecked: Boolean
) : ListItem {
    override val id: String get() = key
}
