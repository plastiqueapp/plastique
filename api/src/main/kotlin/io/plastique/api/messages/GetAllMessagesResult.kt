package io.plastique.api.messages

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetAllMessagesResult(
    @Json(name = "has_more")
    val hasMore: Boolean,

    @Json(name = "cursor")
    val cursor: String,

    @Json(name = "results")
    val results: List<MessageDto>
)
