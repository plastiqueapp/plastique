package io.plastique.deviations

class DeviationNotFoundException(deviationId: String, cause: Throwable) : Exception("Deviation with id '$deviationId' not found", cause)
