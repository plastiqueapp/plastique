package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.collections.FolderDto
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.users.StatusDto
import io.plastique.api.users.UserDto
import org.threeten.bp.ZonedDateTime

sealed class FeedElementDto {
    abstract val timestamp: ZonedDateTime
    abstract val user: UserDto

    @JsonClass(generateAdapter = true)
    data class CollectionUpdate(
        @Json(name = "ts")
        override val timestamp: ZonedDateTime,

        @Json(name = "by_user")
        override val user: UserDto,

        @Json(name = "added_count")
        val addedCount: Int = 0,

        @Json(name = "collection")
        val folder: FolderDto
    ) : FeedElementDto()

    @JsonClass(generateAdapter = true)
    data class DeviationSubmitted(
        @Json(name = "ts")
        override val timestamp: ZonedDateTime,

        @Json(name = "by_user")
        override val user: UserDto,

        @Json(name = "bucketid")
        val bucketId: String? = null,

        @Json(name = "bucket_total")
        val bucketTotal: Int = 0,

        @Json(name = "deviations")
        val deviations: List<DeviationDto>
    ) : FeedElementDto()

    @JsonClass(generateAdapter = true)
    data class JournalSubmitted(
        @Json(name = "ts")
        override val timestamp: ZonedDateTime,

        @Json(name = "by_user")
        override val user: UserDto,

        @Json(name = "deviations")
        val deviations: List<DeviationDto>
    ) : FeedElementDto()

    @JsonClass(generateAdapter = true)
    data class StatusUpdate(
        @Json(name = "ts")
        override val timestamp: ZonedDateTime,

        @Json(name = "by_user")
        override val user: UserDto,

        @Json(name = "status")
        val status: StatusDto
    ) : FeedElementDto()

    @JsonClass(generateAdapter = true)
    data class UsernameChange(
        @Json(name = "ts")
        override val timestamp: ZonedDateTime,

        @Json(name = "by_user")
        override val user: UserDto,

        @Json(name = "formerly")
        val formerName: String
    ) : FeedElementDto()

    object Unknown : FeedElementDto() {
        override val timestamp: ZonedDateTime
            get() = throw NotImplementedError()

        override val user: UserDto
            get() = throw NotImplementedError()
    }
}
