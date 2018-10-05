package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.users.User
import org.threeten.bp.ZonedDateTime

@JsonClass(generateAdapter = true)
data class DailyDeviation(
    @Json(name = "body")
    val body: String,

    @Json(name = "time")
    val date: ZonedDateTime,

    @Json(name = "giver")
    var giver: User
)
