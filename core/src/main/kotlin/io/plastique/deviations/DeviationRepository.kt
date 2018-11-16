package io.plastique.deviations

import io.plastique.api.deviations.DeviationDto
import io.reactivex.Observable
import io.reactivex.Single

interface DeviationRepository {
    fun getDeviationById(deviationId: String): Observable<Deviation>

    fun getDeviationTitleById(deviationId: String): Single<String>

    fun put(deviations: Collection<DeviationDto>)
}
