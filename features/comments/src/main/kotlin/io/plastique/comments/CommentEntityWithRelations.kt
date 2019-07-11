package io.plastique.comments

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

data class CommentEntityWithRelations(
    @Embedded
    val comment: CommentEntity,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val author: UserEntity
)
