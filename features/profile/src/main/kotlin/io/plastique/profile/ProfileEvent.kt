package io.plastique.profile

import com.sch.neon.Event
import io.plastique.core.session.Session

sealed class ProfileEvent : Event() {
    data class SessionChangedEvent(val session: Session) : ProfileEvent()
}
