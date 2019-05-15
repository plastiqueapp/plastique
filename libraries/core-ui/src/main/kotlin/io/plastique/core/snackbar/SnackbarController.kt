package io.plastique.core.snackbar

import android.annotation.SuppressLint
import android.view.View
import com.google.android.material.snackbar.Snackbar

class SnackbarController(private val rootView: View) {
    var onActionClickListener: OnSnackbarActionClickListener? = null

    @SuppressLint("WrongConstant")
    fun showSnackbar(state: SnackbarState) {
        when (state) {
            is SnackbarState.Message -> {
                Snackbar.make(rootView, state.message, Snackbar.LENGTH_LONG).show()
            }
            is SnackbarState.MessageWithAction -> {
                Snackbar.make(rootView, state.message, SNACKBAR_DURATION)
                    .setAction(state.actionText) { onActionClickListener?.invoke(state.actionData) }
                    .show()
            }
        }
    }

    companion object {
        private const val SNACKBAR_DURATION = 5000
    }
}

typealias OnSnackbarActionClickListener = (actionData: Any) -> Unit
