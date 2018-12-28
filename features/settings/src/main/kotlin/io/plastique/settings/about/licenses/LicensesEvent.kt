package io.plastique.settings.about.licenses

import io.plastique.core.flow.Event
import io.plastique.core.lists.ListItem

sealed class LicensesEvent : Event() {
    data class LoadFinishedEvent(val items: List<ListItem>) : LicensesEvent() {
        override fun toString(): String = "LoadFinishedEvent(items=${items.size})"
    }

    data class LoadErrorEvent(val error: Throwable) : LicensesEvent()
}
