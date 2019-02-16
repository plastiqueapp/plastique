package io.plastique.users.profile

import io.plastique.core.flow.Event
import io.plastique.core.session.Session

sealed class UserProfileEvent : Event() {
    data class UserProfileChangedEvent(val userProfile: UserProfile) : UserProfileEvent()
    data class LoadErrorEvent(val error: Throwable) : UserProfileEvent()
    object RetryClickEvent : UserProfileEvent()

    data class SetWatchingEvent(val watching: Boolean) : UserProfileEvent()
    object SetWatchingFinishedEvent : UserProfileEvent()
    data class SetWatchingErrorEvent(val error: Throwable) : UserProfileEvent()

    data class SessionChangedEvent(val session: Session) : UserProfileEvent()
    object CopyProfileLinkClickEvent : UserProfileEvent()
    object SnackbarShownEvent : UserProfileEvent()
    object SignOutEvent : UserProfileEvent()
}
