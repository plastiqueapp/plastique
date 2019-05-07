package io.plastique.deviations.info

import com.sch.neon.Effect

sealed class DeviationInfoEffect : Effect() {
    data class LoadInfoEffect(val deviationId: String) : DeviationInfoEffect()
}
