package io.plastique.core.snackbar

import android.view.View
import com.google.android.material.snackbar.Snackbar

class SnackbarController(private val rootView: View) {
    var onActionClickListener: OnSnackbarActionClickListener? = null

    fun showSnackbar(state: SnackbarState) {
        when (state) {
            is SnackbarState.Message -> {
                Snackbar.make(rootView, state.message, Snackbar.LENGTH_LONG).show()
            }
            is SnackbarState.MessageWithAction -> {
                val snackbar = Snackbar.make(rootView, state.message, Snackbar.LENGTH_LONG)
                snackbar.setAction(state.actionText) { onActionClickListener?.onSnackbarActionClick(state.actionId) }
                snackbar.show()
            }
        }
    }
}
