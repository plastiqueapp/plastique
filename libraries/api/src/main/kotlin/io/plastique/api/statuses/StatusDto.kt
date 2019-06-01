package io.plastique.api.statuses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.NullIfDeleted
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.users.UserDto
import org.threeten.bp.ZonedDateTime

@JsonClass(generateAdapter = true)
data class StatusDto(
    @Json(name = "statusid")
    val id: String,

    @Json(name = "author")
    val author: UserDto,

    @Json(name = "body")
    val body: String,

    @Json(name = "ts")
    val timestamp: ZonedDateTime,

    @Json(name = "url")
    val url: String,

    @Json(name = "comments_count")
    val commentCount: Int = 0,

    @Json(name = "is_share")
    val isShare: Boolean = false,

    @Json(name = "items")
    val items: List<EmbeddedItem> = emptyList()
) {
    sealed class EmbeddedItem {
        @JsonClass(generateAdapter = true)
        data class SharedDeviation(
            @NullIfDeleted
            @Json(name = "deviation")
            val deviation: DeviationDto?
        ) : EmbeddedItem()

        @JsonClass(generateAdapter = true)
        data class SharedStatus(
            @NullIfDeleted
            @Json(name = "status")
            val status: StatusDto?
        ) : EmbeddedItem()

        object Unknown : EmbeddedItem()

        companion object {
            const val TYPE_DEVIATION = "deviation"
            const val TYPE_STATUS = "status"
        }
    }
}
