package io.plastique.core.content

sealed class ContentState {
    object None : ContentState() {
        override fun toString(): String = "None"
    }

    object Content : ContentState() {
        override fun toString(): String = "Content"
    }

    object Loading : ContentState() {
        override fun toString(): String = "Loading"
    }

    data class Empty(val emptyState: EmptyState, val isError: Boolean = false) : ContentState()
}
