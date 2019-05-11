package io.plastique.core.content

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat.requireViewById
import androidx.core.view.isVisible
import io.plastique.util.Animations
import timber.log.Timber
import kotlin.math.max

class ContentStateController(private val onSwitchStateListener: OnSwitchStateListener) {
    private var displayedState: ContentState = ContentState.None
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var switchStateRunnable: Runnable? = null
    private var lastSwitchTime: Long = 0L

    var state: ContentState = ContentState.None
        set(value) {
            if (field != value && !(field is ContentState.Empty && value is ContentState.Empty)) {
                field = value
                switchState(value)
            }
        }

    var progressShowDelay: Long = DEFAULT_PROGRESS_SHOW_DELAY
    var minProgressShowDuration: Long = DEFAULT_MIN_PROGRESS_SHOW_DURATION

    constructor(rootView: View,
                @IdRes contentViewId: Int,
                @IdRes progressViewId: Int = View.NO_ID,
                @IdRes emptyViewId: Int = View.NO_ID)
            : this(ContentStateApplier(rootView, contentViewId, progressViewId, emptyViewId))

    constructor(activity: Activity,
                @IdRes contentViewId: Int,
                @IdRes progressViewId: Int = View.NO_ID,
                @IdRes emptyViewId: Int = View.NO_ID)
            : this(ContentStateApplier(activity, contentViewId, progressViewId, emptyViewId))

    init {
        dispatchSwitchState(displayedState, false)
    }

    private fun switchState(state: ContentState) {
        mainThreadHandler.removeCallbacks(switchStateRunnable)

        var delay = 0L
        if (displayedState === ContentState.Loading) {
            val elapsedTime = SystemClock.elapsedRealtime() - lastSwitchTime
            delay = max(0L, minProgressShowDuration - elapsedTime)
        } else if (state === ContentState.Loading) {
            delay = progressShowDelay
        }

        if (delay != 0L) {
            switchStateRunnable = Runnable { dispatchSwitchState(state, true) }
            mainThreadHandler.postDelayed(switchStateRunnable, delay)
        } else {
            dispatchSwitchState(state, true)
        }
    }

    private fun dispatchSwitchState(state: ContentState, animated: Boolean) {
        Timber.tag(LOG_TAG).d("Switching state to %s", state)
        displayedState = state
        lastSwitchTime = SystemClock.elapsedRealtime()

        onSwitchStateListener.onSwitchState(state, animated)
    }

    interface OnSwitchStateListener {
        fun onSwitchState(state: ContentState, animated: Boolean)
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

    private val contentView: View
    private val progressView: View?
    private val emptyView: View?

    constructor(
        activity: Activity,
        @IdRes contentViewId: Int,
        @IdRes progressViewId: Int,
        @IdRes emptyViewId: Int
    ) : this(activity.window.decorView, contentViewId, progressViewId, emptyViewId)

    init {
        contentView = requireViewById(rootView, contentViewId)
        progressView = if (progressViewId != View.NO_ID) requireViewById<View>(rootView, progressViewId) else null
        emptyView = if (emptyViewId != View.NO_ID) requireViewById<View>(rootView, emptyViewId) else null
    }

    override fun onSwitchState(state: ContentState, animated: Boolean) {
        contentView.setVisible(state === ContentState.Content, animated)
        progressView?.setVisible(state === ContentState.Loading, animated)
        emptyView?.setVisible(state is ContentState.Empty, animated)
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
