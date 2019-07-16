package io.plastique.feed

import io.plastique.collections.folders.Folder
import io.plastique.deviations.Deviation
import io.plastique.statuses.Status
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

sealed class FeedElement {
    abstract val timestamp: ZonedDateTime
    abstract val user: User

    data class CollectionUpdate(
        override val timestamp: ZonedDateTime,
        override val user: User,
        val folder: Folder,
        val addedCount: Int
    ) : FeedElement()

    data class DeviationSubmitted(
        override val timestamp: ZonedDateTime,
        override val user: User,
        val deviation: Deviation
    ) : FeedElement()

    data class MultipleDeviationsSubmitted(
        override val timestamp: ZonedDateTime,
        override val user: User,
        val submittedTotal: Int,
        val deviations: List<Deviation>
    ) : FeedElement()

    data class JournalSubmitted(
        override val timestamp: ZonedDateTime,
        override val user: User,
        val deviation: Deviation
    ) : FeedElement()

    data class StatusUpdate(
        override val timestamp: ZonedDateTime,
        override val user: User,
        val status: Status
    ) : FeedElement()

    data class UsernameChange(
        override val timestamp: ZonedDateTime,
        override val user: User,
        val formerName: String
    ) : FeedElement()
}
