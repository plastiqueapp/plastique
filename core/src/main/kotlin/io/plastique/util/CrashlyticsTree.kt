package io.plastique.util

import com.crashlytics.android.Crashlytics
import io.plastique.core.exceptions.ApiException
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null && !isIgnored(t)) {
            Crashlytics.logException(t)
        }
    }

    private fun isIgnored(e: Throwable): Boolean {
        return e is ApiException
    }
}
