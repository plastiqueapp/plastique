package io.plastique.settings.licenses

import io.plastique.core.content.EmptyState
import io.plastique.core.lists.ListItem

sealed class LicensesViewState {
    override fun toString(): String = "LicensesViewState.${javaClass.simpleName}"

    data class Content(val items: List<ListItem>) : LicensesViewState() {
        override fun toString(): String = "LicensesViewState.Content(items=${items.size})"
    }

    data class Empty(val emptyState: EmptyState) : LicensesViewState() {
        override fun toString(): String = "LicensesViewState.Empty(emptyState=$emptyState)"
    }

    object Loading : LicensesViewState()
}
