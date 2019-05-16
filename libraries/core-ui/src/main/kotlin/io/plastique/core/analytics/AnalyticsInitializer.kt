package io.plastique.core.analytics

import io.plastique.core.init.Initializer
import javax.inject.Inject

class AnalyticsInitializer @Inject constructor(private val analytics: Analytics) : Initializer() {
    override fun initialize() {
        analytics.initUserProperties()
    }
}
