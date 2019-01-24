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

class ContentViewController(
    rootView: View,
    @IdRes contentViewId: Int,
    @IdRes progressViewId: Int = View.NO_ID,
    @IdRes emptyViewId: Int = View.NO_ID
) {
    private val handler = Handler(Looper.getMainLooper())
    private val contentView: View
    private val progressView: View?
    private val emptyView: View?

    private var setStateRunnable: Runnable? = null
    private var lastSwitchTime: Long = 0

    var minProgressDisplayTime: Int = 500
    var progressShowDelay: Int = 200

    private var displayedState: ContentState = ContentState.None
    var state: ContentState = ContentState.None
        set(value) {
            require(value !== ContentState.None) { "Cannot switch state to $value" }
            if (field != value && !(field is ContentState.Empty && value is ContentState.Empty)) {
                field = value
                switchState(value)
            }
        }

    constructor(activity: Activity, @IdRes contentViewId: Int, @IdRes progressViewId: Int = View.NO_ID, @IdRes emptyViewId: Int = View.NO_ID)
            : this(activity.window.decorView, contentViewId, progressViewId, emptyViewId)

    init {
        contentView = requireViewById(rootView, contentViewId)
        progressView = if (progressViewId != View.NO_ID) requireViewById(rootView, progressViewId) else null
        emptyView = if (emptyViewId != View.NO_ID) requireViewById(rootView, emptyViewId) else null
        applyState(ContentState.None, false)
    }

    private fun switchState(state: ContentState) {
        handler.removeCallbacks(setStateRunnable)

        var delay: Long = 0
        if (displayedState === ContentState.Loading) {
            val elapsedTime = SystemClock.elapsedRealtime() - lastSwitchTime
            delay = max(0, minProgressDisplayTime - elapsedTime)
        } else if (state === ContentState.Loading) {
            delay = progressShowDelay.toLong()
        }

        if (delay != 0L) {
            setStateRunnable = Runnable { applyState(state, true) }
            handler.postDelayed(setStateRunnable, delay)
        } else {
            applyState(state, true)
        }
    }

    private fun applyState(state: ContentState, animated: Boolean) {
        Timber.tag(LOG_TAG).d("Applying state %s", state)
        displayedState = state
        lastSwitchTime = SystemClock.elapsedRealtime()

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

    private companion object {
        private const val LOG_TAG = "ContentViewController"
    }
}
