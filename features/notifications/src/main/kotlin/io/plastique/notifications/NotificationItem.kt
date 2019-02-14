package io.plastique.notifications

import io.plastique.core.lists.ListItem
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

abstract class NotificationItem : ListItem {
    abstract val messageId: String
    abstract val time: ZonedDateTime
    abstract val user: User

    override val id: String get() = messageId
}

data class AddToCollectionItem(
    override val messageId: String,
    override val time: ZonedDateTime,
    override val user: User,
    val deviationId: String,
    val deviationTitle: String,
    val folderId: String,
    val folderName: String
) : NotificationItem()

data class FavoriteItem(
    override val messageId: String,
    override val time: ZonedDateTime,
    override val user: User,
    val deviationId: String,
    val deviationTitle: String
) : NotificationItem()

data class WatchItem(
    override val messageId: String,
    override val time: ZonedDateTime,
    override val user: User
) : NotificationItem()
