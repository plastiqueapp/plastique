package io.plastique.profile

import com.sch.neon.Event

sealed class ProfileEvent : Event() {
    data class UserChangedEvent(val userId: String?) : ProfileEvent()
}
