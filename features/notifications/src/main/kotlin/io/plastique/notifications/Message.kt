package io.plastique.notifications

import io.plastique.collections.Folder
import io.plastique.deviations.Deviation
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

sealed class Message {
    abstract val id: String
    abstract val time: ZonedDateTime
    abstract val user: User

    data class AddToCollection(
        override val id: String,
        override val time: ZonedDateTime,
        override val user: User,
        val deviation: Deviation,
        val folder: Folder
    ) : Message()

    data class BadgeGiven(
        override val id: String,
        override val time: ZonedDateTime,
        override val user: User,
        val text: String
    ) : Message()

    data class Favorite(
        override val id: String,
        override val time: ZonedDateTime,
        override val user: User,
        val deviation: Deviation
    ) : Message()

    data class Watch(
        override val id: String,
        override val time: ZonedDateTime,
        override val user: User
    ) : Message()
}
