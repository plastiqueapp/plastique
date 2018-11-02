package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.Deviation
import io.plastique.api.users.Status
import io.plastique.api.users.User
import org.threeten.bp.ZonedDateTime

sealed class FeedElement {
    abstract val timestamp: ZonedDateTime
    abstract val user: User

    companion object {
        const val TYPE_COLLECTION_UPDATE = "collection_update"
        const val TYPE_DEVIATION_SUBMITTED = "deviation_submitted"
        const val TYPE_JOURNAL_SUBMITTED = "journal_submitted"
        const val TYPE_STATUS = "status"
        const val TYPE_USERNAME_CHANGE = "username_change"
    }
}

@JsonClass(generateAdapter = true)
data class CollectionUpdateElement(
    @Json(name = "ts")
    override val timestamp: ZonedDateTime,

    @Json(name = "by_user")
    override val user: User,

    @Json(name = "added_count")
    val addedCount: Int = 0,

    @Json(name = "collection")
    val collection: CollectionInfo
) : FeedElement() {
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
data class DeviationSubmittedElement(
    @Json(name = "ts")
    override val timestamp: ZonedDateTime,

    @Json(name = "by_user")
    override val user: User,

    @Json(name = "bucketid")
    val bucketId: String? = null,

    @Json(name = "bucket_total")
    val bucketSize: Int = 0,

    @Json(name = "deviations")
    val deviations: List<Deviation>
) : FeedElement()

@JsonClass(generateAdapter = true)
data class JournalSubmittedElement(
    @Json(name = "ts")
    override val timestamp: ZonedDateTime,

    @Json(name = "by_user")
    override val user: User,

    @Json(name = "deviations")
    val deviations: List<Deviation>
) : FeedElement()

@JsonClass(generateAdapter = true)
data class StatusElement(
    @Json(name = "ts")
    override val timestamp: ZonedDateTime,

    @Json(name = "by_user")
    override val user: User,

    @Json(name = "status")
    val status: Status
) : FeedElement()

@JsonClass(generateAdapter = true)
data class UsernameChangeElement(
    @Json(name = "ts")
    override val timestamp: ZonedDateTime,

    @Json(name = "by_user")
    override val user: User,

    @Json(name = "formerly")
    val formerName: String
) : FeedElement()
