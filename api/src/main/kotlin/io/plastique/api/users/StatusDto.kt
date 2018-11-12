package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.ZonedDateTime

@JsonClass(generateAdapter = true)
data class StatusDto(
    @Json(name = "statusid")
    val id: String,

    @Json(name = "author")
    val author: UserDto,

    @Json(name = "body")
    val body: String,

    @Json(name = "ts")
    val timestamp: ZonedDateTime,

    @Json(name = "url")
    val url: String,

    @Json(name = "is_deleted")
    val isDeleted: Boolean = false,

    @Json(name = "is_share")
    val isShare: Boolean = false,

    @Json(name = "comments_count")
    val commentCount: Int = 0
)
