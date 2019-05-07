package io.plastique.deviations.info

import com.sch.neon.Event

sealed class DeviationInfoEvent : Event() {
    data class DeviationInfoChangedEvent(val deviationInfo: DeviationInfo) : DeviationInfoEvent()
    data class LoadErrorEvent(val error: Throwable) : DeviationInfoEvent()
    object RetryClickEvent : DeviationInfoEvent()
}
