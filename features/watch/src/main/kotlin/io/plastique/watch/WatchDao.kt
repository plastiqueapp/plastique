package io.plastique.watch

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface WatchDao {
    @Transaction
    @Query("SELECT * FROM watchers WHERE `key` = :key ORDER BY `order`")
    fun getWatchers(key: String): List<WatcherWithUser>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertWatchers(watchers: Collection<WatcherEntity>)

    @Query("DELETE FROM watchers WHERE `key` = :key")
    fun deleteWatchersByKey(key: String)

    @Query("SELECT coalesce(max(`order`), 0) FROM watchers WHERE `key` = :key")
    fun getMaxOrder(key: String): Int
}
