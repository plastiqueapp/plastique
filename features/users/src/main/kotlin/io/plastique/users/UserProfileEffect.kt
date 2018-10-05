package io.plastique.users

import io.plastique.core.flow.Effect

sealed class UserProfileEffect : Effect() {
    data class LoadUserProfileEffect(val username: String) : UserProfileEffect()
    data class CopyProfileLinkEffect(val profileUrl: String) : UserProfileEffect()
}
