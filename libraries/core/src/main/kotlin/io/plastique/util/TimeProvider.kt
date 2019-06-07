package io.plastique.util

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

interface TimeProvider {
    val currentInstant: Instant

    val currentTime: ZonedDateTime

    val timeZone: ZoneId
}

object SystemTimeProvider : TimeProvider {
    override val currentInstant: Instant
        get() = Instant.now()

    override val currentTime: ZonedDateTime
        get() = ZonedDateTime.now()

    override val timeZone: ZoneId
        get() = ZoneId.systemDefault()
}
