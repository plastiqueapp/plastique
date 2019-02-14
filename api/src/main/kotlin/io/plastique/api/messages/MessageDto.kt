package io.plastique.api.messages

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.comments.CommentDto
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.users.StatusDto
import io.plastique.api.users.UserDto
import org.threeten.bp.ZonedDateTime
import io.plastique.api.collections.FolderDto as CollectionFolderDto
import io.plastique.api.gallery.FolderDto as GalleryFolderDto

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "messageid")
    val id: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "orphaned")
    val isOrphaned: Boolean = false,

    @Json(name = "ts")
    val timestamp: ZonedDateTime?,

    @Json(name = "stackid")
    val stackId: String? = null,

    @Json(name = "stack_count")
    val stackCount: Int = 0,

    @Json(name = "originator")
    val originator: UserDto?,

    @Json(name = "subject")
    val subject: Subject?,

    @Json(name = "html")
    val html: String?,

    @Json(name = "deviation")
    val deviation: DeviationDto?,

    @Json(name = "profile")
    val profile: UserDto?,

    @Json(name = "comment")
    val comment: CommentDto?,

    @Json(name = "status")
    val status: StatusDto?,

    @Json(name = "collection")
    val collection: CollectionFolderDto?
) {
    @JsonClass(generateAdapter = true)
    data class Subject(
        @Json(name = "deviation")
        val deviation: DeviationDto?,

        @Json(name = "profile")
        val profile: UserDto?,

        @Json(name = "comment")
        val comment: CommentDto?,

        @Json(name = "status")
        val status: StatusDto?,

        @Json(name = "collection")
        val collection: CollectionFolderDto?,

        @Json(name = "gallery")
        val gallery: GalleryFolderDto?
    )
}
