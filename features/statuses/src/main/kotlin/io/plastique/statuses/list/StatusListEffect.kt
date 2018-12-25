package io.plastique.statuses.list

import io.plastique.core.flow.Effect

sealed class StatusListEffect : Effect() {
    data class LoadStatusesEffect(val username: String) : StatusListEffect()
    object LoadMoreEffect : StatusListEffect()
    object RefreshEffect : StatusListEffect()
}
