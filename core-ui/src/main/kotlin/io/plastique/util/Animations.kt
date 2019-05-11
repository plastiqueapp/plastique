package io.plastique.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object Animations {
    const val DURATION_SHORT: Long = 300

    fun fadeIn(view: View, duration: Long) {
        view.animate()
            .alpha(1.0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                }
            })
    }

    fun fadeOut(view: View, duration: Long) {
        view.animate()
            .alpha(0.0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
    }
}
