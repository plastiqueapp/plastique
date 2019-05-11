package io.plastique.feed

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.deviations.DeviationEntity
import io.plastique.deviations.DeviationImageEntity
import io.plastique.users.UserEntity

@DatabaseView("""SELECT deviations.*, feed_deviations.feed_element_id FROM deviations
INNER JOIN feed_deviations ON deviations.id = feed_deviations.deviation_id
ORDER BY feed_deviations.feed_element_id, feed_deviations.`order`""",
    viewName = "feed_deviations_ordered")
data class FeedDeviationEntityWithRelations(
    @ColumnInfo(name = "feed_element_id")
    val feedElementId: Long,

    @Embedded
    val deviation: DeviationEntity,

    @Relation(parentColumn = "author_id", entityColumn = "id")
    val author: List<UserEntity>,

    @Relation(parentColumn = "daily_deviation_giver_id", entityColumn = "id")
    val dailyDeviationGiver: List<UserEntity>,

    @Relation(parentColumn = "id", entityColumn = "deviation_id")
    val images: List<DeviationImageEntity>
)
