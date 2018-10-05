package io.plastique.users

import androidx.room.Embedded
import androidx.room.Relation

class UserProfileWithUser {
    @Embedded
    lateinit var userProfile: UserProfileEntity

    @Relation(parentColumn = "user_id", entityColumn = "id")
    var user: List<UserEntity> = emptyList()
}
