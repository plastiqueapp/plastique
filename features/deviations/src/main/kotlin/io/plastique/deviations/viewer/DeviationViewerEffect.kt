package io.plastique.deviations.viewer

import io.plastique.core.flow.Effect

sealed class DeviationViewerEffect : Effect() {
    data class LoadDeviationEffect(val deviationId: String) : DeviationViewerEffect()
    data class DownloadOriginalEffect(val deviationId: String) : DeviationViewerEffect()
}
