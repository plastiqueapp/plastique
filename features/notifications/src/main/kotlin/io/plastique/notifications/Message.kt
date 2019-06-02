package io.plastique.notifications

import io.plastique.collections.Folder
import io.plastique.deviations.Deviation
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class Message(
    val id: String,
    val time: ZonedDateTime,
    val user: User,
    val data: Data
) {
    sealed class Data {
        data class AddToCollection(
            val deviation: Deviation,
            val folder: Folder
        ) : Data()

        data class BadgeGiven(
            val text: String
        ) : Data()

        data class Favorite(
            val deviation: Deviation
        ) : Data()

        object Watch : Data() {
            override fun toString(): String = "Watch"
        }
    }
}
