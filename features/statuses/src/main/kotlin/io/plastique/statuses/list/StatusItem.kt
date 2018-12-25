package io.plastique.statuses.list

import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.statuses.ShareUiModel
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class StatusItem(
    val statusId: String,
    val author: User,
    val date: ZonedDateTime,
    val statusText: SpannedWrapper,
    val commentCount: Int,
    val share: ShareUiModel
) : ListItem {
    override val id: String get() = statusId
}
