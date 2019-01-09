package io.plastique.deviations.info

import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class DeviationInfo(
    val title: String,
    val author: User,
    val publishTime: ZonedDateTime,
    val description: String,
    val tags: List<String>
)
