package io.plastique.users.profile

import com.sch.neon.Event

sealed class UserProfileEvent : Event() {
    data class UserProfileChangedEvent(val userProfile: UserProfile) : UserProfileEvent()
    data class LoadErrorEvent(val error: Throwable) : UserProfileEvent()
    object RetryClickEvent : UserProfileEvent()

    data class SetWatchingEvent(val watching: Boolean) : UserProfileEvent()
    object SetWatchingFinishedEvent : UserProfileEvent()
    data class SetWatchingErrorEvent(val error: Throwable) : UserProfileEvent()

    data class UserChangedEvent(val userId: String?) : UserProfileEvent()
    object CopyProfileLinkClickEvent : UserProfileEvent()
    object SnackbarShownEvent : UserProfileEvent()
    object SignOutEvent : UserProfileEvent()
}
