package io.plastique.users.profile

import com.sch.neon.Effect

sealed class UserProfileEffect : Effect() {
    data class LoadUserProfileEffect(val username: String) : UserProfileEffect()
    data class CopyProfileLinkEffect(val profileUrl: String) : UserProfileEffect()
    data class SetWatchingEffect(val username: String, val watching: Boolean) : UserProfileEffect()

    object SignOutEffect : UserProfileEffect()

    sealed class NavigationEffect : UserProfileEffect() {
        data class OpenBrowserEffect(val url: String) : NavigationEffect()
        object OpenSignInEffect : NavigationEffect()
    }
}
