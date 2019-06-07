package io.plastique.feed

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import io.plastique.deviations.DeviationEntityWithRelations

@DatabaseView("""SELECT deviations.*, feed_deviations.feed_element_id FROM deviations
INNER JOIN feed_deviations ON deviations.id = feed_deviations.deviation_id
ORDER BY feed_deviations.feed_element_id, feed_deviations.`order`""",
    viewName = "feed_deviations_ordered")
data class FeedDeviationEntityWithRelations(
    @ColumnInfo(name = "feed_element_id")
    val feedElementId: Long,

    @Embedded
    val deviationEntityWithRelations: DeviationEntityWithRelations
)
