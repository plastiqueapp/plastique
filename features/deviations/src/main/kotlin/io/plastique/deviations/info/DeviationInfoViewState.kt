package io.plastique.deviations.info

import com.github.technoir42.kotlin.extensions.truncate
import io.plastique.core.content.EmptyState
import io.plastique.core.text.SpannedWrapper
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

sealed class DeviationInfoViewState {
    abstract val deviationId: String

    data class Loading(
        override val deviationId: String
    ) : DeviationInfoViewState() {
        override fun toString(): String = "DeviationInfoViewState.Loading(deviationId=$deviationId)"
    }

    data class Content(
        override val deviationId: String,
        val title: String,
        val author: User,
        val publishTime: ZonedDateTime,
        val description: SpannedWrapper,
        val tags: List<String>
    ) : DeviationInfoViewState() {

        @Suppress("MagicNumber")
        override fun toString(): String {
            return "DeviationInfoViewState.Content(" +
                    "deviationId=$deviationId, " +
                    "title=$title, " +
                    "author=$author, " +
                    "publishTime=$publishTime, " +
                    "description=${description.toString().truncate(20)}, " +
                    "tags=$tags" +
                    ")"
        }
    }

    data class Error(
        override val deviationId: String,
        val emptyViewState: EmptyState
    ) : DeviationInfoViewState() {
        override fun toString(): String = "DeviationInfoViewState.Error(deviationId=$deviationId, emptyViewState=$emptyViewState)"
    }
}
