package io.plastique.settings.about.licenses

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem

data class LicensesViewState(
    val contentState: ContentState,
    val items: List<ListItem> = emptyList()
) {
    override fun toString(): String = "LicensesViewState(contentState=$contentState, items=${items.size})"
}
