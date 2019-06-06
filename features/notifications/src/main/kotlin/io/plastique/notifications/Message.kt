package io.plastique.notifications

import io.plastique.collections.Folder
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
            val deviationId: String,
            val deviationTitle: String,
            val folder: Folder
        ) : Data()

        data class BadgeGiven(
            val text: String
        ) : Data()

        data class Favorite(
            val deviationId: String,
            val deviationTitle: String
        ) : Data()

        object Watch : Data() {
            override fun toString(): String = "Watch"
        }
    }
}
