package io.plastique.core.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseTracker(context: Context) : Tracker {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
