package io.plastique.deviations

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

data class DeviationEntityWithRelations(
    @Embedded
    val deviation: DeviationEntity,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val author: List<UserEntity>,

    @Relation(entity = DailyDeviationEntity::class, parentColumn = "id", entityColumn = "deviation_id")
    val dailyDeviation: List<DailyDeviationEntityWithRelations>,

    @Relation(parentColumn = "id", entityColumn = "deviation_id")
    val images: List<DeviationImageEntity>,

    @Relation(parentColumn = "id", entityColumn = "deviation_id")
    val videos: List<DeviationVideoEntity>
)
