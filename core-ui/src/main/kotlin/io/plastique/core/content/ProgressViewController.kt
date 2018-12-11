package io.plastique.core.content

import android.app.Activity
import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import kotlin.math.max

class ProgressViewController(private val progressView: View) {
    private val showDelay: Long = 250
    private val minShowTime: Long = 500
    private var lastVisibilityChangeTimestamp: Long = 0
    private var setVisibilityRunnable: Runnable? = null

    var isVisible: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                setProgressBarVisible(value)
            }
        }

    init {
        progressView.isVisible = false
    }

    constructor(activity: Activity, @IdRes progressViewId: Int) : this(activity.findViewById(progressViewId))
    constructor(rootView: View, @IdRes progressViewId: Int) : this(rootView.findViewById(progressViewId))

    private fun setProgressBarVisible(visible: Boolean) {
        progressView.removeCallbacks(setVisibilityRunnable)

        val delay: Long = if (visible) {
            showDelay
        } else {
            val elapsedTime = SystemClock.elapsedRealtime() - lastVisibilityChangeTimestamp
            max(minShowTime - elapsedTime, 0)
        }

        if (delay != 0L) {
            setVisibilityRunnable = Runnable { applyVisibility(visible) }
            progressView.postDelayed(setVisibilityRunnable, delay)
        } else {
            applyVisibility(visible)
        }
    }

    private fun applyVisibility(visible: Boolean) {
        progressView.isVisible = visible
        lastVisibilityChangeTimestamp = SystemClock.elapsedRealtime()
    }
}
