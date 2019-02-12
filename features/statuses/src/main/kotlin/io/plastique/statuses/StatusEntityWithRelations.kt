package io.plastique.statuses

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.deviations.DeviationEntity
import io.plastique.deviations.DeviationEntityWithRelations
import io.plastique.deviations.toDeviation
import io.plastique.users.UserEntity
import io.plastique.users.toUser

data class StatusEntityWithRelations(
    @Embedded
    val status: StatusEntity,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val users: List<UserEntity>,

    @Relation(entity = DeviationEntity::class, parentColumn = "shared_deviation_id", entityColumn = "id")
    val deviations: List<DeviationEntityWithRelations>,

    @Relation(entity = StatusEntity::class, parentColumn = "shared_status_id", entityColumn = "id")
    val statuses: List<StatusEntityWithUsers>
)

data class StatusEntityWithUsers(
    @Embedded
    val status: StatusEntity,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val users: List<UserEntity>
)

fun StatusEntityWithRelations.toStatus(): Status {
    val share = when (status.shareType) {
        ShareType.None -> Status.Share.None
        ShareType.Deviation -> Status.Share.DeviationShare(deviation = deviations.firstOrNull()?.toDeviation())
        ShareType.Status -> Status.Share.StatusShare(status = statuses.firstOrNull()?.toStatus())
    }
    return Status(
            id = status.id,
            date = status.timestamp,
            body = status.body,
            author = users.first().toUser(),
            commentCount = status.commentCount,
            share = share)
}

private fun StatusEntityWithUsers.toStatus(): Status = Status(
        id = status.id,
        date = status.timestamp,
        body = status.body,
        author = users.first().toUser(),
        commentCount = status.commentCount,
        share = Status.Share.None)
