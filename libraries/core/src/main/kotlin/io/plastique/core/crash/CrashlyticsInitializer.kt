package io.plastique.core.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.plastique.core.init.Initializer

class CrashlyticsInitializer(
    private val debug: Boolean
) : Initializer() {
    override fun initialize() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!debug)
    }
}
