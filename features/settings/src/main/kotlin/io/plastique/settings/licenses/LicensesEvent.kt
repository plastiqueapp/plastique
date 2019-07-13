package io.plastique.settings.licenses

import com.sch.neon.Event
import io.plastique.core.lists.ListItem

sealed class LicensesEvent : Event() {
    data class LoadFinishedEvent(val items: List<ListItem>) : LicensesEvent() {
        override fun toString(): String = "LoadFinishedEvent(items=${items.size})"
    }

    data class LoadErrorEvent(val error: Throwable) : LicensesEvent()
    object RetryClickEvent : LicensesEvent()
}
