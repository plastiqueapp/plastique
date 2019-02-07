package io.plastique.main

import io.plastique.core.flow.Event
import io.plastique.users.User

sealed class MainEvent : Event() {
    data class UserChangedEvent(val user: User?) : MainEvent()
}
