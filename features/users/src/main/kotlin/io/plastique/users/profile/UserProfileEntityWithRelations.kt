package io.plastique.users.profile

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

data class UserProfileEntityWithRelations(
    @Embedded
    val userProfile: UserProfileEntity,

    @Relation(parentColumn = "user_id", entityColumn = "id")
    val user: UserEntity
)
