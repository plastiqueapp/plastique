package io.plastique.core.content

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat.requireViewById
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import io.plastique.util.Animations
import timber.log.Timber
import java.util.UUID
import kotlin.math.max

class ContentStateController private constructor() {
    private val onSwitchStateListeners = mutableListOf<OnSwitchStateListener>()
    private var displayedState: ContentState = ContentState.None
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var switchStateRunnable: Runnable? = null
    private var lastSwitchTime: Long = 0L

    var state: ContentState = ContentState.None
        set(value) {
            if (field != value) {
                field = value
                switchState(value)
            }
        }

    var progressShowDelay: Long = DEFAULT_PROGRESS_SHOW_DELAY
    var minProgressShowDuration: Long = DEFAULT_MIN_PROGRESS_SHOW_DURATION

    init {
        dispatchSwitchState(displayedState, false)
    }

    constructor(
        fragment: Fragment,
        @IdRes contentViewId: Int,
        @IdRes progressViewId: Int = View.NO_ID,
        @IdRes emptyViewId: Int = View.NO_ID
    ) : this() {
        addListener(ContentStateApplier(fragment.requireView(), contentViewId, progressViewId, emptyViewId))
        registerIdlingResource(fragment.viewLifecycleOwner, fragment.javaClass.simpleName)
    }

    constructor(
        activity: FragmentActivity,
        @IdRes contentViewId: Int,
        @IdRes progressViewId: Int = View.NO_ID,
        @IdRes emptyViewId: Int = View.NO_ID
    ) : this() {
        addListener(ContentStateApplier(activity.window.decorView, contentViewId, progressViewId, emptyViewId))
        registerIdlingResource(activity, activity.javaClass.simpleName)
    }

    fun addListener(listener: OnSwitchStateListener) {
        onSwitchStateListeners += listener
    }

    fun removeListener(listener: OnSwitchStateListener) {
        onSwitchStateListeners -= listener
    }

    private fun switchState(state: ContentState) {
        switchStateRunnable?.let { mainThreadHandler.removeCallbacks(it) }

        var delay = 0L
        if (displayedState == ContentState.Loading) {
            val elapsedTime = SystemClock.elapsedRealtime() - lastSwitchTime
            delay = max(0L, minProgressShowDuration - elapsedTime)
        } else if (state == ContentState.Loading) {
            delay = progressShowDelay
        }

        if (delay != 0L) {
            val delayedRunnable = Runnable { dispatchSwitchState(state, true) }
            mainThreadHandler.postDelayed(delayedRunnable, delay)
            switchStateRunnable = delayedRunnable
        } else {
            dispatchSwitchState(state, true)
        }
    }

    private fun dispatchSwitchState(state: ContentState, animated: Boolean) {
        Timber.tag(LOG_TAG).d("Switching state to %s", state)
        displayedState = state
        lastSwitchTime = SystemClock.elapsedRealtime()
        onSwitchStateListeners.forEach { it.onSwitchState(state, animated) }
    }

    private fun registerIdlingResource(lifecycleOwner: LifecycleOwner, ownerName: String) {
        val name = "ContentStateController_${ownerName}_${UUID.randomUUID()}"
        val idlingResource = IdlingResourceImpl(name, this)

        val idlingRegistry = IdlingRegistry.getInstance()
        idlingRegistry.register(idlingResource)
        addListener(idlingResource)

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                lifecycleOwner.lifecycle.removeObserver(this)
                idlingRegistry.unregister(idlingResource)
                removeListener(idlingResource)
            }
        })
    }

    interface OnSwitchStateListener {
        fun onSwitchState(state: ContentState, animated: Boolean)
    }

    private class IdlingResourceImpl(
        private val name: String,
        private val contentStateController: ContentStateController
    ) : IdlingResource, OnSwitchStateListener {
        private var callback: IdlingResource.ResourceCallback? = null
        private var idle: Boolean = contentStateController.displayedState.isIdle

        override fun getName(): String = name

        override fun isIdleNow(): Boolean =
            contentStateController.displayedState.isIdle

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
            this.callback = callback
        }

        override fun onSwitchState(state: ContentState, animated: Boolean) {
            val idle = state.isIdle
            if (!this.idle && idle) {
                callback?.onTransitionToIdle()
            }
            this.idle = idle
        }

        private val ContentState.isIdle: Boolean
            get() = this != ContentState.Loading
    }

    private companion object {
        private const val LOG_TAG = "ContentStateController"
        private const val DEFAULT_PROGRESS_SHOW_DELAY = 200L
        private const val DEFAULT_MIN_PROGRESS_SHOW_DURATION = 500L
    }
}

private class ContentStateApplier(
    rootView: View,
    @IdRes contentViewId: Int,
    @IdRes progressViewId: Int,
    @IdRes emptyViewId: Int
) : ContentStateController.OnSwitchStateListener {

    private val contentView = requireViewById<View>(rootView, contentViewId)
    private val progressView = if (progressViewId != View.NO_ID) requireViewById<View>(rootView, progressViewId) else null
    private val emptyView = if (emptyViewId != View.NO_ID) requireViewById<View>(rootView, emptyViewId) else null

    override fun onSwitchState(state: ContentState, animated: Boolean) {
        contentView.setVisible(state == ContentState.Content, animated)
        progressView?.setVisible(state == ContentState.Loading, animated)
        emptyView?.setVisible(state == ContentState.Empty, animated)
    }

    private fun View.setVisible(visible: Boolean, animated: Boolean) {
        if (animated) {
            if (visible) {
                Animations.fadeIn(this, Animations.DURATION_SHORT)
            } else {
                Animations.fadeOut(this, Animations.DURATION_SHORT)
            }
        } else {
            isVisible = visible
        }
    }
}
