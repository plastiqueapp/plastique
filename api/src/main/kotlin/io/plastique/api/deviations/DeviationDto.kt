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
    var url: String,

    @Json(name = "is_downloadable")
    var isDownloadable: Boolean = false,

    @Json(name = "is_favourited")
    var isFavorite: Boolean = false,

    @Json(name = "is_mature")
    var isMature: Boolean = false,

    @Json(name = "content")
    var content: ImageDto? = null,

    @Json(name = "preview")
    var preview: ImageDto? = null,

    @Json(name = "excerpt")
    var excerpt: String? = null,

    @Json(name = "author")
    var author: UserDto,

    @Json(name = "daily_deviation")
    var dailyDeviation: DailyDeviationDto? = null
)
