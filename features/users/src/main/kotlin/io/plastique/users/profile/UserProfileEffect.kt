package io.plastique.users.profile

import com.sch.neon.Effect

sealed class UserProfileEffect : Effect() {
    data class LoadUserProfileEffect(val username: String) : UserProfileEffect()
    data class CopyProfileLinkEffect(val profileUrl: String) : UserProfileEffect()
    data class SetWatchingEffect(val username: String, val watching: Boolean) : UserProfileEffect()

    object OpenSignInEffect : UserProfileEffect()
    object SignOutEffect : UserProfileEffect()
}
