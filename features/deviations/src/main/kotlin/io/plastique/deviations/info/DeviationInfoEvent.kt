package io.plastique.deviations.info

import io.plastique.core.flow.Event

sealed class DeviationInfoEvent : Event() {
    data class DeviationInfoChangedEvent(val deviationInfo: DeviationInfo) : DeviationInfoEvent()
    data class LoadErrorEvent(val error: Throwable) : DeviationInfoEvent()
    object RetryClickEvent : DeviationInfoEvent()
}
