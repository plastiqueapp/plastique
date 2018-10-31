package io.plastique.core.snackbar

sealed class SnackbarState {
    object None : SnackbarState() {
        override fun toString(): String = "None"
    }

    data class Message(val message: CharSequence) : SnackbarState()

    data class MessageWithAction(
        val message: CharSequence,
        val actionId: Int,
        val actionText: CharSequence
    ) : SnackbarState()
}
