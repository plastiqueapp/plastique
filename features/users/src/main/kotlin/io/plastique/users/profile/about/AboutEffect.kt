package io.plastique.users.profile.about

import com.sch.neon.Effect

sealed class AboutEffect : Effect() {
    data class LoadEffect(val username: String) : AboutEffect()
}
