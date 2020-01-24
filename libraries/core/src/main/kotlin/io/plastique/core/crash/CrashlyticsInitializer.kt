package io.plastique.core.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.plastique.core.init.Initializer
import timber.log.Timber

class CrashlyticsInitializer(
    private val debug: Boolean
) : Initializer() {
    override fun initialize() {
        val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        firebaseCrashlytics.setCrashlyticsCollectionEnabled(!debug)

        Timber.plant(CrashlyticsTree(firebaseCrashlytics))
    }
}
