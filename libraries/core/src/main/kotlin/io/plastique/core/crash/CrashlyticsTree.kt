package io.plastique.core.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.io.IOException

internal class CrashlyticsTree(
    private val firebaseCrashlytics: FirebaseCrashlytics
) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null && !t.isIgnored) {
            firebaseCrashlytics.recordException(t)
        }
    }

    private val Throwable.isIgnored: Boolean
        get() = this is IOException
}
