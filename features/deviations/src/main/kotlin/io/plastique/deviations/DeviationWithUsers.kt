package io.plastique.deviations

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

data class DeviationWithUsers(
    @Embedded
    val deviation: DeviationEntity,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val author: List<UserEntity>,

    @Relation(parentColumn = "daily_deviation_giver_id", entityColumn = "id")
    val dailyDeviationGiver: List<UserEntity>
)
