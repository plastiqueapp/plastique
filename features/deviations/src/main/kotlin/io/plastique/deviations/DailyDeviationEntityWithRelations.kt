package io.plastique.deviations

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

data class DailyDeviationEntityWithRelations(
    @Embedded
    val dailyDeviation: DailyDeviationEntity,

    @Relation(parentColumn = "giver_id", entityColumn = "id")
    val giver: List<UserEntity>
)
