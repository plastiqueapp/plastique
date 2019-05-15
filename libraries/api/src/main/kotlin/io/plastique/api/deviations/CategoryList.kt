package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryList(
    @Json(name = "categories")
    val categories: List<CategoryDto>
)
