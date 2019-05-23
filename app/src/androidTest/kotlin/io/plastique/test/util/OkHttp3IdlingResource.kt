package io.plastique.test.util

import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient

class OkHttp3IdlingResource(client: OkHttpClient, private val name: String) : IdlingResource {
    private val dispatcher = client.dispatcher()
    @Volatile private var callback: IdlingResource.ResourceCallback? = null
    @Volatile private var idle: Boolean = true

    init {
        dispatcher.setIdleCallback {
            idle = true
            callback?.onTransitionToIdle()
        }
    }

    override fun getName(): String = name

    override fun isIdleNow(): Boolean {
        val idle = dispatcher.runningCallsCount() == 0
        if (idle && !this.idle) {
            callback?.onTransitionToIdle()
        }
        this.idle = idle
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
    }
}
