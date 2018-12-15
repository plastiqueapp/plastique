package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "user")
    val user: UserDto,

    @Json(name = "profile_url")
    val profileUrl: String,

    @Json(name = "real_name")
    val realName: String?,

    @Json(name = "is_watching")
    val isWatching: Boolean
)
