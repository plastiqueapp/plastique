package io.plastique.feed

import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class CollectionUpdateItem(
    override val id: String,
    val date: ZonedDateTime,
    val user: User,
    val folderId: String,
    val folderName: String,
    val addedCount: Int,
    val folderItems: List<ListItem>
) : ListItem

abstract class DeviationItem : ListItem {
    abstract val deviation: Deviation

    override val id: String get() = deviation.id
}

data class ImageDeviationItem(
    val date: ZonedDateTime,
    val user: User,
    override val deviation: Deviation
) : DeviationItem()

data class LiteratureDeviationItem(
    val date: ZonedDateTime,
    val user: User,
    override val deviation: Deviation,
    val excerpt: SpannedWrapper
) : DeviationItem()

data class MultipleDeviationsItem(
    override val id: String,
    val date: ZonedDateTime,
    val user: User,
    val submittedTotal: Int,
    val items: List<ListItem>
) : ListItem

data class StatusUpdateItem(
    val date: ZonedDateTime,
    val user: User,
    val statusId: String,
    val text: SpannedWrapper,
    val commentCount: Int,
    val sharedItem: StatusSharedItem
) : ListItem {
    override val id: String get() = statusId
}

sealed class StatusSharedItem {
    object None : StatusSharedItem()

    data class ImageDeviation(
        val deviationId: String,
        val author: User,
        val title: String,
        val preview: Deviation.Image
    ) : StatusSharedItem()

    data class LiteratureDeviation(
        val deviationId: String,
        val author: User,
        val title: String,
        val excerpt: SpannedWrapper
    ) : StatusSharedItem()

    data class Status(
        val statusId: String,
        val author: User,
        val date: ZonedDateTime,
        val text: SpannedWrapper
    ) : StatusSharedItem()

    object DeletedDeviation : StatusSharedItem()
    object DeletedStatus : StatusSharedItem()
}

data class UsernameChangeItem(
    override val id: String,
    val date: ZonedDateTime,
    val user: User,
    val formerName: String
) : ListItem

val StatusSharedItem.isDeleted: Boolean
    get() = this === StatusSharedItem.DeletedDeviation || this === StatusSharedItem.DeletedStatus
