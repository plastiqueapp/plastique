package io.plastique.core.time

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

object SystemTimeProvider : TimeProvider {
    override val currentInstant: Instant
        get() = Instant.now()

    override val currentTime: ZonedDateTime
        get() = ZonedDateTime.now()

    override val timeZone: ZoneId
        get() = ZoneId.systemDefault()
}
