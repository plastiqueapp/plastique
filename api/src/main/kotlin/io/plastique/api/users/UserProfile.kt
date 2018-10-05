package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "user")
    val user: User,

    @Json(name = "profile_url")
    val profileUrl: String,

    @Json(name = "real_name")
    val realName: String?
)
