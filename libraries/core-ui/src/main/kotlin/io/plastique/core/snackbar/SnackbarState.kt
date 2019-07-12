package io.plastique.core.snackbar

import androidx.annotation.StringRes

sealed class SnackbarState {
    data class Message(
        @StringRes val messageResId: Int,
        val messageArgs: List<Any> = emptyList()
    ) : SnackbarState()

    data class MessageWithAction(
        @StringRes val messageResId: Int,
        val messageArgs: List<Any> = emptyList(),
        @StringRes val actionTextId: Int,
        val actionData: Any
    ) : SnackbarState()
}
