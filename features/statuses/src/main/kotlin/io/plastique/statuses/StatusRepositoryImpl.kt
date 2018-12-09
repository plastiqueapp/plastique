package io.plastique.statuses

import androidx.room.RoomDatabase
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.users.StatusDto
import io.plastique.api.users.UserDto
import io.plastique.deviations.DeviationRepository
import io.plastique.users.UserRepository
import javax.inject.Inject

class StatusRepositoryImpl @Inject constructor(
    private val database: RoomDatabase,
    private val statusDao: StatusDao,
    private val deviationRepository: DeviationRepository,
    private val userRepository: UserRepository
) : StatusRepository {

    override fun put(statuses: Collection<StatusDto>) {
        if (statuses.isEmpty()) {
            return
        }

        val deviations = mutableListOf<DeviationDto>()
        val users = mutableListOf<UserDto>()
        val flattenedStatuses = mutableListOf<StatusEntity>()
        statuses.forEach { status -> collectEntities(status, flattenedStatuses, deviations, users) }

        val uniqueDeviations = deviations.distinctBy { deviation -> deviation.id }
        val uniqueUsers = users.distinctBy { user -> user.id }
        val uniqueStatuses = flattenedStatuses.distinctBy { status -> status.id }
        database.runInTransaction {
            userRepository.put(uniqueUsers)
            deviationRepository.put(uniqueDeviations)
            statusDao.insertOrUpdate(uniqueStatuses)
        }
    }

    private fun collectEntities(status: StatusDto, statuses: MutableCollection<StatusEntity>, deviations: MutableCollection<DeviationDto>, users: MutableCollection<UserDto>) {
        var shareType: ShareType = ShareType.None
        var sharedDeviationId: String? = null
        var sharedStatusId: String? = null
        users += status.author

        status.items.forEach { item ->
            when (item) {
                is StatusDto.EmbeddedItem.SharedDeviation -> {
                    shareType = ShareType.Deviation
                    item.deviation?.let { deviation ->
                        deviations += deviation
                        sharedDeviationId = deviation.id
                    }
                }

                is StatusDto.EmbeddedItem.SharedStatus -> {
                    shareType = ShareType.Status
                    item.status?.let { status ->
                        collectEntities(status, statuses, deviations, users)
                        sharedStatusId = status.id
                    }
                }
            }
        }

        statuses += status.toStatusEntity(
                shareType = shareType,
                sharedDeviationId = sharedDeviationId,
                sharedStatusId = sharedStatusId)
    }
}

private fun StatusDto.toStatusEntity(shareType: ShareType, sharedDeviationId: String?, sharedStatusId: String?): StatusEntity {
    return StatusEntity(
            id = id,
            body = body,
            timestamp = timestamp,
            url = url,
            authorId = author.id,
            commentCount = commentCount,
            shareType = shareType,
            sharedDeviationId = sharedDeviationId,
            sharedStatusId = sharedStatusId)
}
