package io.plastique.deviations.list

import io.plastique.core.flow.Effect
import io.plastique.deviations.FetchParams

sealed class DeviationListEffect : Effect() {
    data class LoadDeviationsEffect(val params: FetchParams) : DeviationListEffect()
    object LoadMoreEffect : DeviationListEffect()
    object RefreshEffect : DeviationListEffect()
    data class SetFavoriteEffect(val deviationId: String, val favorite: Boolean) : DeviationListEffect()
}
