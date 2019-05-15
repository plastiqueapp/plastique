package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "user")
    val user: UserDto,

    @Json(name = "profile_url")
    val url: String,

    @Json(name = "real_name")
    val realName: String?,

    @Json(name = "bio")
    val bio: String?,

    @Json(name = "is_watching")
    val isWatching: Boolean,

    @Json(name = "stats")
    val stats: Stats
) {
    @JsonClass(generateAdapter = true)
    data class Stats(
        @Json(name = "user_deviations")
        val userDeviations: Int,

        @Json(name = "user_favourites")
        val userFavorites: Int
    )
}
