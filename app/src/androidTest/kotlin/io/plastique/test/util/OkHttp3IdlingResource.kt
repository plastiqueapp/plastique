package io.plastique.test.util

import androidx.test.espresso.IdlingResource
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

class OkHttp3IdlingResource(private val dispatcher: Dispatcher, private val name: String) : IdlingResource {
    @Volatile private var callback: IdlingResource.ResourceCallback? = null
    @Volatile private var idle: Boolean = true

    constructor(okHttpClient: OkHttpClient, name: String) : this(okHttpClient.dispatcher, name)

    init {
        dispatcher.idleCallback = Runnable {
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
