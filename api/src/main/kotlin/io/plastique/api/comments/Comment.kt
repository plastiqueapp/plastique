package io.plastique.api.comments

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.users.User
import org.threeten.bp.ZonedDateTime

@JsonClass(generateAdapter = true)
data class Comment(
    @Json(name = "commentid")
    val id: String,

    @Json(name = "parentid")
    val parentId: String? = null,

    @Json(name = "posted")
    val datePosted: ZonedDateTime,

    @Json(name = "replies")
    val numReplies: Int = 0,

    @Json(name = "hidden")
    val hidden: String? = null,

    @Json(name = "body")
    val text: String,

    @Json(name = "user")
    var author: User
)
