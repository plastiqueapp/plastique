package io.plastique.users.profile.about

import com.sch.neon.Event
import io.plastique.users.profile.UserProfile

sealed class AboutEvent : Event() {
    data class UserProfileChangedEvent(val userProfile: UserProfile) : AboutEvent()
    data class LoadErrorEvent(val error: Throwable) : AboutEvent()
    object RetryClickEvent : AboutEvent()
}
