package io.plastique.statuses

import io.plastique.api.users.StatusDto

interface StatusRepository {
    fun put(statuses: Collection<StatusDto>)
}
