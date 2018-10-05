package io.plastique.deviations

import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class DailyDeviation(
    val body: String,
    val date: ZonedDateTime,
    val giver: User
)
