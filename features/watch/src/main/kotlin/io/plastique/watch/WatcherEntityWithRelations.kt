package io.plastique.watch

import androidx.room.Embedded
import androidx.room.Relation
import io.plastique.users.UserEntity

data class WatcherEntityWithRelations(
    @Embedded
    val watcher: WatcherEntity,

    @Relation(parentColumn = "user_id", entityColumn = "id")
    val users: List<UserEntity>
)
