package io.plastique.core.content

import androidx.annotation.StringRes

sealed class EmptyState {
    data class Message(
        @StringRes val messageResId: Int,
        val messageArgs: List<Any> = emptyList()
    ) : EmptyState()

    data class MessageWithButton(
        @StringRes val messageResId: Int,
        val messageArgs: List<Any> = emptyList(),
        @StringRes val buttonTextId: Int
    ) : EmptyState()
}
