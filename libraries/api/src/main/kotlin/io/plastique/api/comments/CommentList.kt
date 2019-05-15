package io.plastique.api.comments

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentList(
    @Json(name = "has_more")
    val hasMore: Boolean = false,

    @Json(name = "next_offset")
    val nextOffset: Int? = 0,

    @Json(name = "thread")
    val comments: List<CommentDto> = emptyList()
)
