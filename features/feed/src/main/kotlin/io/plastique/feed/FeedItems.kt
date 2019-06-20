package io.plastique.feed

import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.statuses.ShareUiModel
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

sealed class FeedListItem : ListItem {
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

sealed class DeviationItem : FeedListItem() {
    abstract val deviationId: String
    abstract val title: String
    abstract val isFavorite: Boolean
    abstract val allowsComments: Boolean
    abstract val favoriteCount: Int
    abstract val commentCount: Int

    override val id: String get() = deviationId
}

data class ImageDeviationItem(
    override val date: ZonedDateTime,
    override val user: User,
    override val deviationId: String,
    override val title: String,
    override val isFavorite: Boolean,
    override val allowsComments: Boolean,
    override val favoriteCount: Int,
    override val commentCount: Int,
    val content: Deviation.ImageInfo?,
    val preview: Deviation.ImageInfo
) : DeviationItem()

data class LiteratureDeviationItem(
    override val date: ZonedDateTime,
    override val user: User,
    override val deviationId: String,
    override val title: String,
    override val isFavorite: Boolean,
    override val allowsComments: Boolean,
    override val favoriteCount: Int,
    override val commentCount: Int,
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
