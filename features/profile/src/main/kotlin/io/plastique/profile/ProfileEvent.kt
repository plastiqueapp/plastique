package io.plastique.profile

import io.plastique.core.flow.Event
import io.plastique.core.session.Session

sealed class ProfileEvent : Event() {
    data class SessionChangedEvent(val session: Session) : ProfileEvent()
}
