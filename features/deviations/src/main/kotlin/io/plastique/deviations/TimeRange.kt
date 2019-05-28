package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class TimeRange {
    @Json(name = "8hr")
    Hours8,
    @Json(name = "24hr")
    Hours24,
    @Json(name = "3days")
    Days3,
    @Json(name = "1week")
    Week,
    @Json(name = "1month")
    Month,
    @Json(name = "alltime")
    AllTime
}
