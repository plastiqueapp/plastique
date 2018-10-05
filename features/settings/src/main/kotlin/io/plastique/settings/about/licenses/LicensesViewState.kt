package io.plastique.settings.about.licenses

import io.plastique.core.content.ContentState

data class LicensesViewState(
    val contentState: ContentState,
    val items: List<LicensesItem> = emptyList()
) {
    override fun toString(): String = "LicensesViewState(contentState=$contentState, items=${items.size})"
}
