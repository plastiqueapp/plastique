package io.plastique.inject.components

import io.plastique.inject.BaseAppComponent
import io.plastique.util.Preferences
import okhttp3.OkHttpClient

interface AppComponent : BaseAppComponent {
    fun okHttpClient(): OkHttpClient

    fun preferences(): Preferences

    override fun createActivityComponent(): ActivityComponent
}
