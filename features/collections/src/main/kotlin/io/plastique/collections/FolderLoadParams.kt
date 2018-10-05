package io.plastique.collections

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FolderLoadParams(
    @Json(name = "username")
    val username: String?,

    @Json(name = "show_mature")
    val matureContent: Boolean
)
