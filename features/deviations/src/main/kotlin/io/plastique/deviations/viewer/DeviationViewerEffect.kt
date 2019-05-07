package io.plastique.deviations.viewer

import com.sch.neon.Effect

sealed class DeviationViewerEffect : Effect() {
    data class LoadDeviationEffect(val deviationId: String) : DeviationViewerEffect()
    data class DownloadOriginalEffect(val deviationId: String) : DeviationViewerEffect()
    data class SetFavoriteEffect(val deviationId: String, val favorite: Boolean) : DeviationViewerEffect()
}
