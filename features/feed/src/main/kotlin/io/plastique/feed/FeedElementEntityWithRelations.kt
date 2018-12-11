package io.plastique.feed

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.collections.FolderEntity
import io.plastique.statuses.StatusEntity
import io.plastique.statuses.StatusEntityWithRelations
import io.plastique.users.UserEntity

data class FeedElementEntityWithRelations(
    @Embedded
    val feedElement: FeedElementEntity,

    @Relation(parentColumn = "user_id", entityColumn = "id")
    val users: List<UserEntity>,

    @Relation(parentColumn = "folder_id", entityColumn = "id")
    val collectionFolders: List<FolderEntity>,

    @Relation(parentColumn = "id", entityColumn = "feed_element_id")
    val deviations: List<FeedDeviationEntityWithRelations>,

    @Relation(entity = StatusEntity::class, parentColumn = "status_id", entityColumn = "id")
    val statuses: List<StatusEntityWithRelations>
)
