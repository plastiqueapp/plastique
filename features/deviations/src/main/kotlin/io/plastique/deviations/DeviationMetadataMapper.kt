package io.plastique.deviations

import io.plastique.api.deviations.DeviationMetadata
import javax.inject.Inject

class DeviationMetadataMapper @Inject constructor() {
    fun map(metadata: DeviationMetadata): DeviationMetadataEntity {
        return DeviationMetadataEntity(
                deviationId = metadata.deviationId,
                description = metadata.description)
    }
}
