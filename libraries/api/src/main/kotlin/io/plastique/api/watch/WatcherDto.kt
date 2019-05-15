package io.plastique.api.watch

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.users.UserDto
import org.threeten.bp.ZonedDateTime

@JsonClass(generateAdapter = true)
data class WatcherDto(
    @Json(name = "user")
    val user: UserDto,

    @Json(name = "is_watching")
    val isWatching: Boolean,

    @Json(name = "lastvisit")
    val lastVisit: ZonedDateTime? = null,

    @Json(name = "watch")
    val watchSettings: WatchSettings
) {
    @JsonClass(generateAdapter = true)
    data class WatchSettings(
        @Json(name = "friend")
        val friend: Boolean,

        @Json(name = "deviations")
        val deviations: Boolean,

        @Json(name = "journals")
        val journals: Boolean,

        @Json(name = "forum_threads")
        val forumThreads: Boolean,

        @Json(name = "critiques")
        val critiques: Boolean,

        @Json(name = "scraps")
        val scraps: Boolean,

        @Json(name = "activity")
        val activity: Boolean,

        @Json(name = "collections")
        val collections: Boolean
    )
}
