package io.plastique.util

import com.crashlytics.android.Crashlytics
import timber.log.Timber
import java.io.IOException

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null && !t.isIgnored) {
            Crashlytics.logException(t)
        }
    }

    private val Throwable.isIgnored: Boolean
        get() = this is IOException
}
