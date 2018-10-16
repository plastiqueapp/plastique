package io.plastique.deviations

import io.plastique.users.User
import io.plastique.users.UserEntity
import io.plastique.users.toUser
import org.threeten.bp.ZonedDateTime

data class DailyDeviation(
    val body: String,
    val date: ZonedDateTime,
    val giver: User
)

fun DailyDeviationEntity.toDailyDeviation(giver: UserEntity): DailyDeviation {
    if (giverId != giver.id) {
        throw IllegalArgumentException("Expected user with id $giverId but got ${giver.id}")
    }
    return DailyDeviation(body = body, date = date, giver = giver.toUser())
}
