package io.plastique.users.profile.about

import com.github.technoir42.kotlin.extensions.truncate
import io.plastique.core.content.EmptyState
import io.plastique.core.text.SpannedWrapper

sealed class AboutViewState {
    abstract val username: String

    data class Content(
        override val username: String,
        val bio: SpannedWrapper
    ) : AboutViewState() {

        @Suppress("MagicNumber")
        override fun toString(): String {
            return "Content(" +
                    "username=$username, " +
                    "bio=${bio.toString().truncate(20)}" +
                    ")"
        }
    }

    data class Loading(
        override val username: String
    ) : AboutViewState()

    data class Error(
        override val username: String,
        val emptyState: EmptyState
    ) : AboutViewState()
}
