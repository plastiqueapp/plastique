package io.plastique.core.snackbar

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class SnackbarController(private val rootView: View) {
    private val shownSnackbars = mutableSetOf<Snackbar>()
    var onActionClickListener: OnSnackbarActionClickListener? = null

    constructor(fragment: Fragment, rootView: View) : this(rootView) {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                if (!fragment.requireActivity().isChangingConfigurations) {
                    dismissSnackbars()
                }
                fragment.lifecycle.removeObserver(this)
            }
        })
    }

    @SuppressLint("WrongConstant")
    fun showSnackbar(state: SnackbarState) {
        when (state) {
            is SnackbarState.Message -> {
                val message = getMessageWithArgs(state.messageResId, state.messageArgs)
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                    .addCallback(snackbarCallback)
                    .show()
            }

            is SnackbarState.MessageWithAction -> {
                val message = getMessageWithArgs(state.messageResId, state.messageArgs)
                Snackbar.make(rootView, message, SNACKBAR_DURATION)
                    .setAction(state.actionTextId) { onActionClickListener?.invoke(state.actionData) }
                    .addCallback(snackbarCallback)
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

    private fun dismissSnackbars() {
        shownSnackbars.forEach { it.dismiss() }
    }

    private val snackbarCallback = object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onShown(snackbar: Snackbar) {
            shownSnackbars += snackbar
        }

        override fun onDismissed(snackbar: Snackbar, event: Int) {
            shownSnackbars -= snackbar
        }
    }

    companion object {
        private const val SNACKBAR_DURATION = 10000
    }
}

typealias OnSnackbarActionClickListener = (actionData: Any) -> Unit
