package io.plastique.api.collections

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FolderList(
    @Json(name = "has_more")
    val hasMore: Boolean,

    @Json(name = "next_offset")
    val nextOffset: Int? = 0,

    @Json(name = "results")
    val folders: List<Folder> = emptyList()
)
