package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
        val collection: CollectionInfo
    ) : FeedElementDto() {
        @JsonClass(generateAdapter = true)
        data class CollectionInfo(
            @Json(name = "folderid")
            val folderId: String,

            @Json(name = "name")
            val name: String,

            @Json(name = "url")
            val url: String,

            @Json(name = "size")
            val size: Int = 0
        )
    }

    @JsonClass(generateAdapter = true)
    data class DeviationSubmitted(
        @Json(name = "ts")
        override val timestamp: ZonedDateTime,

        @Json(name = "by_user")
        override val user: UserDto,

        @Json(name = "bucketid")
        val bucketId: String? = null,

        @Json(name = "bucket_total")
        val bucketSize: Int = 0,

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

    companion object {
        const val TYPE_COLLECTION_UPDATE = "collection_update"
        const val TYPE_DEVIATION_SUBMITTED = "deviation_submitted"
        const val TYPE_JOURNAL_SUBMITTED = "journal_submitted"
        const val TYPE_STATUS_UPDATE = "status"
        const val TYPE_USERNAME_CHANGE = "username_change"
    }
}
