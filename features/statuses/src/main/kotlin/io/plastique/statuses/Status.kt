package io.plastique.statuses

import io.plastique.deviations.Deviation
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class Status(
    val id: String,
    val date: ZonedDateTime,
    val body: String,
    val author: User,
    val share: Share,
    val commentCount: Int
) {
    val shareObjectId: ShareObjectId?
        get() = when (share) {
            Share.None -> ShareObjectId.Status(id)
            is Share.DeviationShare -> share.deviation?.let { ShareObjectId.Deviation(it.id, id) }
            is Share.StatusShare -> share.status?.let { ShareObjectId.Status(it.id, id) }
        }

    sealed class Share {
        object None : Share() {
            override fun toString(): String = "None"
        }

        data class DeviationShare(val deviation: Deviation?) : Share()
        data class StatusShare(val status: Status?) : Share()
    }
}
