package io.plastique.statuses

import io.plastique.api.statuses.StatusDto

interface StatusRepository {
    fun put(statuses: Collection<StatusDto>)
}
