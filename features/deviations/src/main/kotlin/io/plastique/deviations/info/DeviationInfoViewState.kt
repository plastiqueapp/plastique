package io.plastique.deviations.info

import io.plastique.core.content.EmptyState
import io.plastique.core.text.SpannedWrapper
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

sealed class DeviationInfoViewState {
    abstract val deviationId: String

    data class Loading(
        override val deviationId: String
    ) : DeviationInfoViewState()

    data class Content(
        override val deviationId: String,
        val title: String,
        val author: User,
        val publishTime: ZonedDateTime,
        val description: SpannedWrapper,
        val tags: List<String>
    ) : DeviationInfoViewState()

    data class Error(
        override val deviationId: String,
        val emptyViewState: EmptyState
    ) : DeviationInfoViewState()
}
