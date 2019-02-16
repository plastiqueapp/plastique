package io.plastique.users.profile

import io.plastique.core.flow.Effect

sealed class UserProfileEffect : Effect() {
    data class LoadUserProfileEffect(val username: String) : UserProfileEffect()
    data class CopyProfileLinkEffect(val profileUrl: String) : UserProfileEffect()
    data class SetWatchingEffect(val username: String, val watching: Boolean) : UserProfileEffect()
    object SignOutEffect : UserProfileEffect()
}
