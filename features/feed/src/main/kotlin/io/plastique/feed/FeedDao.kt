package io.plastique.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FeedDao {
    @Transaction
    @Query("SELECT * FROM feed ORDER BY timestamp DESC")
    fun getFeed(): List<FeedElementEntityWithRelations>

    @Insert
    fun insert(feedElements: Collection<FeedElementEntity>): LongArray

    @Query("DELETE FROM feed")
    fun deleteAll()

    @Insert
    fun insertDeviationLinks(links: Collection<FeedElementDeviation>)
}
