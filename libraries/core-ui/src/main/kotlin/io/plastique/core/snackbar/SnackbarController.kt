package io.plastique.core.snackbar

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar

class SnackbarController(private val rootView: View) {
    private val shownSnackbars = mutableSetOf<Snackbar>()
    private var snackbarState: SnackbarState? = null
    var onActionClick: OnSnackbarActionClickListener = {}
    var onSnackbarShown: OnSnackbarShownListener = {}

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
        if (state.id == snackbarState?.id) {
            return
        }

        snackbarState = state
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
                    .setAction(state.actionTextId) { onActionClick(state.actionData) }
                    .addCallback(snackbarCallback)
                    .show()
            }
        }
        onSnackbarShown()
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

    private val snackbarCallback = object : Snackbar.Callback() {
        override fun onShown(snackbar: Snackbar) {
            shownSnackbars += snackbar
        }

        override fun onDismissed(snackbar: Snackbar, event: Int) {
            shownSnackbars -= snackbar
            snackbarState = null
        }
    }

    companion object {
        private const val SNACKBAR_DURATION = 10000
    }
}

typealias OnSnackbarActionClickListener = (actionData: Any) -> Unit
typealias OnSnackbarShownListener = () -> Unit
