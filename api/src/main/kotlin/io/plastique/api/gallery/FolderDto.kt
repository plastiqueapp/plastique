package io.plastique.api.gallery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.DeviationDto

@JsonClass(generateAdapter = true)
data class FolderDto(
    @Json(name = "folderid")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "parent")
    val parentId: String? = null,

    @Json(name = "size")
    val size: Int = 0,

    @Json(name = "deviations")
    val deviations: List<DeviationDto> = emptyList()
)
