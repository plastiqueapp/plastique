package io.plastique.comments

import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class Comment(
    val id: String,
    val parentId: String?,
    val author: User,
    val datePosted: ZonedDateTime,
    val text: String
)
