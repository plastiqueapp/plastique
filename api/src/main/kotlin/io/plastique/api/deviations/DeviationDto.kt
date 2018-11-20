package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.ImageDto
import io.plastique.api.users.UserDto

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

    @Json(name = "daily_deviation")
    val dailyDeviation: DailyDeviationDto? = null
)
