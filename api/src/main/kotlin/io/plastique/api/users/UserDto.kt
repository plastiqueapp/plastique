package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "userid")
    val id: String,

    @Json(name = "username")
    val name: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "usericon")
    val avatarUrl: String
)
