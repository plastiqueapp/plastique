package io.plastique.util

import android.app.Activity
import android.view.View

class SystemUiController(private val activity: Activity) {
    var isVisible: Boolean
        get() = (activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0
        set(value) {
            var flags = View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            if (!value) {
                flags = flags or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }
            activity.window.decorView.systemUiVisibility = flags
        }

    fun toggleVisibility() {
        isVisible = !isVisible
    }

    fun setVisibilityChangeListener(listener: (visible: Boolean) -> Unit) {
        activity.window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            listener(visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
        }
    }
}
