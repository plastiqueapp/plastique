package io.plastique.core.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CrashlyticsTree @Inject constructor() : Timber.Tree() {
    private val firebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null && !t.isIgnored) {
            firebaseCrashlytics.recordException(t)
        }
    }

    private val Throwable.isIgnored: Boolean
        get() = this is IOException
}
