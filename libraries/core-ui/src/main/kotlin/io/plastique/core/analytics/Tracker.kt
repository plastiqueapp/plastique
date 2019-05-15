package io.plastique.core.analytics

interface Tracker {
    fun setUserProperty(name: String, value: String?)
}
