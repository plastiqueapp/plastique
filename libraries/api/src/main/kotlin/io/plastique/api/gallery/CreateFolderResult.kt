package io.plastique.api.gallery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateFolderResult(
    @Json(name = "folderid")
    val folderId: String,

    @Json(name = "name")
    val name: String
)
