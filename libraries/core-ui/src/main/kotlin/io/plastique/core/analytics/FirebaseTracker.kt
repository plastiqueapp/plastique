package io.plastique.core.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class FirebaseTracker @Inject constructor(context: Context) : Tracker {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
