package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "catpath")
    val path: String,

    @Json(name = "parent_catpath")
    val parent: String?,

    @Json(name = "title")
    val title: String,

    @Json(name = "has_subcategory")
    val hasChildren: Boolean
)
