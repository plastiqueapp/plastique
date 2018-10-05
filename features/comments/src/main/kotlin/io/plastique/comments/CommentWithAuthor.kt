package io.plastique.comments

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

class CommentWithAuthor {
    @Embedded
    lateinit var comment: CommentEntity

    @Relation(parentColumn = "author_id", entityColumn = "id")
    lateinit var authors: List<UserEntity>
}
