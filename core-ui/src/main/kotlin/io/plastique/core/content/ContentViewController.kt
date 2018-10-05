package io.plastique.core.content

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import io.plastique.util.Animations
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

    private var currentState: ContentState? = null
    private var setStateRunnable: Runnable? = null
    private var lastSwitchTime: Long = 0

    var minProgressDisplayTime = 500
    var progressShowDelay = 200

    constructor(activity: Activity, @IdRes contentViewId: Int, @IdRes progressViewId: Int = View.NO_ID, @IdRes emptyViewId: Int = View.NO_ID)
            : this(activity.window.decorView, contentViewId, progressViewId, emptyViewId)

    init {
        contentView = findViewById(rootView, contentViewId)
        progressView = if (progressViewId != View.NO_ID) findViewById(rootView, progressViewId) else null
        emptyView = if (emptyViewId != View.NO_ID) findViewById(rootView, emptyViewId) else null
        setState(ContentState.None, false)
    }

    fun switchState(state: ContentState) {
        handler.removeCallbacks(setStateRunnable)
        if (currentState == state) {
            return
        }

        var delay: Long = 0
        if (currentState === ContentState.Loading) {
            val elapsedTime = SystemClock.elapsedRealtime() - lastSwitchTime
            delay = max(0, minProgressDisplayTime - elapsedTime)
        } else if (state === ContentState.Loading) {
            delay = progressShowDelay.toLong()
        }

        if (delay != 0L) {
            setStateRunnable = Runnable { setState(state, true) }
            handler.postDelayed(setStateRunnable, delay)
        } else {
            setState(state, true)
        }
    }

    private fun setState(state: ContentState, animated: Boolean) {
        currentState = state
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

    private fun findViewById(rootView: View, @IdRes viewId: Int): View {
        return rootView.findViewById(viewId) ?: throw IllegalArgumentException("View with id $viewId doesn't exist")
    }
}
