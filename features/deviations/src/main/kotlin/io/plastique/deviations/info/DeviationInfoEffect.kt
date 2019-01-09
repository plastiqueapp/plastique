package io.plastique.deviations.info

import io.plastique.core.flow.Effect

sealed class DeviationInfoEffect : Effect() {
    data class LoadInfoEffect(val deviationId: String) : DeviationInfoEffect()
}
