package io.plastique.settings.about.licenses

import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event

sealed class LicensesEvent : Event() {
    data class LoadFinishedEvent(val items: List<LicensesItem>) : LicensesEvent() {
        override fun toString(): String = "LoadFinishedEvent(items=${items.size})"
    }

    data class LoadErrorEvent(val emptyState: EmptyState) : LicensesEvent()
}
