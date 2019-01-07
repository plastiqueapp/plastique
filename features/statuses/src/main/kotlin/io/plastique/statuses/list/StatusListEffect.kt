package io.plastique.statuses.list

import io.plastique.core.flow.Effect
import io.plastique.statuses.StatusListLoadParams

sealed class StatusListEffect : Effect() {
    data class LoadStatusesEffect(val params: StatusListLoadParams) : StatusListEffect()
    object LoadMoreEffect : StatusListEffect()
    object RefreshEffect : StatusListEffect()
}
