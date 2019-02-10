package io.plastique.comments

import io.plastique.api.comments.CommentDto

interface CommentRepository {
    fun put(comments: Collection<CommentDto>)
}
