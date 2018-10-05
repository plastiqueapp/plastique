package io.plastique.deviations

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

class DeviationWithUsers {
    @Embedded
    lateinit var deviation: DeviationEntity

    @Relation(parentColumn = "author_id", entityColumn = "id")
    var author: List<UserEntity> = emptyList()

    @Relation(parentColumn = "daily_deviation_giver_id", entityColumn = "id")
    var dailyDeviationGiver: List<UserEntity> = emptyList()
}
