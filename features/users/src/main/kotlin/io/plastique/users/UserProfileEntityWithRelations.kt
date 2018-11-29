package io.plastique.users

import androidx.room.Embedded
import androidx.room.Relation

data class UserProfileEntityWithRelations(
    @Embedded
    val userProfile: UserProfileEntity,

    @Relation(parentColumn = "user_id", entityColumn = "id")
    val users: List<UserEntity>
)
