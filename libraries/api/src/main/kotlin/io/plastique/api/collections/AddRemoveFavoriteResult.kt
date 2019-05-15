package io.plastique.api.collections

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddRemoveFavoriteResult(
    @Json(name = "favourites")
    val numFavorites: Int
)
