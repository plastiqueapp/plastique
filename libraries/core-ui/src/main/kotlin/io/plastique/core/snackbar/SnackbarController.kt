package io.plastique.core.snackbar

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar

class SnackbarController(private val rootView: View) {
    var onActionClickListener: OnSnackbarActionClickListener? = null

    @SuppressLint("WrongConstant")
    fun showSnackbar(state: SnackbarState) {
        when (state) {
            is SnackbarState.Message -> {
                val message = getMessageWithArgs(state.messageResId, state.messageArgs)
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
            }

            is SnackbarState.MessageWithAction -> {
                val message = getMessageWithArgs(state.messageResId, state.messageArgs)
                Snackbar.make(rootView, message, SNACKBAR_DURATION)
                    .setAction(state.actionTextId) { onActionClickListener?.invoke(state.actionData) }
                    .show()
            }
        }
    }

    private fun getMessageWithArgs(@StringRes messageResId: Int, args: List<Any>): CharSequence {
        val html = if (args.isNotEmpty()) {
            rootView.resources.getString(messageResId, *args.toTypedArray())
        } else {
            rootView.resources.getString(messageResId)
        }
        return HtmlCompat.fromHtml(html, 0)
    }

    companion object {
        private const val SNACKBAR_DURATION = 5000
    }
}

typealias OnSnackbarActionClickListener = (actionData: Any) -> Unit
