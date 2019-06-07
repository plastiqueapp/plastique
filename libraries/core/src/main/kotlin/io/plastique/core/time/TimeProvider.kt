package io.plastique.core.time

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

interface TimeProvider {
    val currentInstant: Instant

    val currentTime: ZonedDateTime

    val timeZone: ZoneId
}
