package io.plastique.feed

import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.statuses.ShareUiModel
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

abstract class FeedListItem : ListItem {
    abstract val date: ZonedDateTime
    abstract val user: User
}

data class CollectionUpdateItem(
    override val id: String,
    override val date: ZonedDateTime,
    override val user: User,
    val folderId: String,
    val folderName: String,
    val addedCount: Int,
    val folderItems: List<ListItem>
) : FeedListItem()

abstract class DeviationItem : FeedListItem() {
    abstract val deviation: Deviation

    override val id: String get() = deviation.id
}

data class ImageDeviationItem(
    override val date: ZonedDateTime,
    override val user: User,
    override val deviation: Deviation
) : DeviationItem()

data class LiteratureDeviationItem(
    override val date: ZonedDateTime,
    override val user: User,
    override val deviation: Deviation,
    val excerpt: SpannedWrapper
) : DeviationItem()

data class MultipleDeviationsItem(
    override val id: String,
    override val date: ZonedDateTime,
    override val user: User,
    val submittedTotal: Int,
    val items: List<ListItem>
) : FeedListItem()

data class StatusUpdateItem(
    override val date: ZonedDateTime,
    override val user: User,
    val statusId: String,
    val text: SpannedWrapper,
    val commentCount: Int,
    val share: ShareUiModel
) : FeedListItem() {
    override val id: String get() = statusId
}

data class UsernameChangeItem(
    override val id: String,
    override val date: ZonedDateTime,
    override val user: User,
    val formerName: String
) : FeedListItem()
