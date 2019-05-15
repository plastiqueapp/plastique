package io.plastique.util

import org.threeten.bp.Instant

interface TimeProvider {
    val currentInstant: Instant
}

object SystemTimeProvider : TimeProvider {
    override val currentInstant: Instant
        get() = Instant.now()
}
