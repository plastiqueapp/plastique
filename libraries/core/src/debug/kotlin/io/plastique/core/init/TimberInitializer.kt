package io.plastique.core.init

import timber.log.Timber
import javax.inject.Inject

class TimberInitializer @Inject constructor() : Initializer() {
    override fun initialize() {
        Timber.plant(Timber.DebugTree())
    }

    @Suppress("MagicNumber")
    override val priority: Int get() = 10
}
