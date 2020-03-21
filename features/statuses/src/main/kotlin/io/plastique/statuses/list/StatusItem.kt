package io.plastique.statuses.list

import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.statuses.StatusActionsState
import io.plastique.statuses.share.ShareUiModel
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class StatusItem(
    val statusId: String,
    val author: User,
    val date: ZonedDateTime,
    val statusText: SpannedWrapper,
    val actionsState: StatusActionsState,
    val share: ShareUiModel
) : ListItem {
    override val id: String get() = statusId
}
