package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.ImageDto
import io.plastique.api.users.UserDto
import org.threeten.bp.ZonedDateTime

@JsonClass(generateAdapter = true)
data class DeviationDto(
    @Json(name = "deviationid")
    val id: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "url")
    val url: String,

    @Json(name = "is_downloadable")
    val isDownloadable: Boolean = false,

    @Json(name = "is_favourited")
    val isFavorite: Boolean = false,

    @Json(name = "is_mature")
    val isMature: Boolean = false,

    @Json(name = "allows_comments")
    val allowsComments: Boolean = true,

    @Json(name = "content")
    val content: ImageDto? = null,

    @Json(name = "preview")
    val preview: ImageDto? = null,

    @Json(name = "excerpt")
    val excerpt: String? = null,

    @Json(name = "author")
    val author: UserDto,

    @Json(name = "stats")
    val stats: Stats,

    @Json(name = "daily_deviation")
    val dailyDeviation: DailyDeviation? = null
) {
    @JsonClass(generateAdapter = true)
    data class DailyDeviation(
        @Json(name = "body")
        val body: String,

        @Json(name = "time")
        val date: ZonedDateTime,

        @Json(name = "giver")
        val giver: UserDto
    )

    @JsonClass(generateAdapter = true)
    data class Stats(
        @Json(name = "comments")
        val comments: Int,

        @Json(name = "favourites")
        val favorites: Int
    )
}
