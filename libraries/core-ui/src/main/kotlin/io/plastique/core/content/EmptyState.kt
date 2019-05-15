package io.plastique.core.content

sealed class EmptyState {
    data class Message(
        val message: CharSequence
    ) : EmptyState()

    data class MessageWithButton(
        val message: CharSequence,
        val button: CharSequence
    ) : EmptyState()
}
