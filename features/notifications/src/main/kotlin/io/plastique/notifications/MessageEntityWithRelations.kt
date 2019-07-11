package io.plastique.notifications

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.collections.FolderEntity
import io.plastique.deviations.DeviationEntity
import io.plastique.deviations.DeviationEntityWithRelations
import io.plastique.users.UserEntity

data class MessageEntityWithRelations(
    @Embedded
    val message: MessageEntity,

    @Relation(parentColumn = "originator_id", entityColumn = "id")
    val originator: UserEntity,

    @Relation(entity = DeviationEntity::class, parentColumn = "deviation_id", entityColumn = "id")
    val deviation: DeviationEntityWithRelations?,

    @Relation(parentColumn = "collection_folder_id", entityColumn = "id")
    val collectionFolder: FolderEntity?,

    @Relation(entity = DeviationEntity::class, parentColumn = "subject_deviation_id", entityColumn = "id")
    val subjectDeviation: DeviationEntityWithRelations?
)
