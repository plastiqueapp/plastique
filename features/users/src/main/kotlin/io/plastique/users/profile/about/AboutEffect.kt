package io.plastique.users.profile.about

import io.plastique.core.flow.Effect

sealed class AboutEffect : Effect() {
    data class LoadEffect(val username: String) : AboutEffect()
}
