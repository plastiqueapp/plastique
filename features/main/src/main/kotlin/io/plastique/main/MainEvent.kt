package io.plastique.main

import com.sch.neon.Event
import io.plastique.users.User

sealed class MainEvent : Event() {
    data class UserChangedEvent(val user: User?) : MainEvent()
}
