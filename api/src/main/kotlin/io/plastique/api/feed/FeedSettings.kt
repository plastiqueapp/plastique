package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedSettings(
    @Json(name = "include")
    val include: Map<String, Boolean>
) {
    companion object {
        const val COLLECTIONS = "collections"
        const val DEVIATIONS = "deviations"
        const val GROUP_DEVIATIONS = "group_deviations"
        const val JOURNALS = "journals"
        const val STATUSES = "statuses"
        const val MISC = "misc"
    }
}
