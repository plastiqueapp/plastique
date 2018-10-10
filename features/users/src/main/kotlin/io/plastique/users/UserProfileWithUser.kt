package io.plastique.users

import androidx.room.Embedded
import androidx.room.Relation

data class UserProfileWithUser(
    @Embedded
    val userProfile: UserProfileEntity,

    @Relation(parentColumn = "user_id", entityColumn = "id")
    val user: List<UserEntity>
)
