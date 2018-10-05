package io.plastique.users

import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event

sealed class UserProfileEvent : Event() {
    data class UserProfileChangedEvent(val userProfile: UserProfile) : UserProfileEvent()
    data class LoadErrorEvent(val emptyState: EmptyState) : UserProfileEvent()

    object RetryClickEvent : UserProfileEvent()
    object CopyProfileLinkClickEvent : UserProfileEvent()
    object LinkCopiedMessageShownEvent : UserProfileEvent()
}
