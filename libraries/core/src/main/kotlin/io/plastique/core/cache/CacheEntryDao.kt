package io.plastique.core.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface CacheEntryDao {
    @Query("SELECT * FROM cache_entries WHERE `key` = :key")
    fun getEntryByKey(key: String): CacheEntryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entry: CacheEntryEntity): Long

    @Update
    fun update(entry: CacheEntryEntity)

    @Transaction
    fun insertOrUpdate(entry: CacheEntryEntity) {
        if (insert(entry) == -1L) {
            update(entry)
        }
    }

    @Query("DELETE FROM cache_entries WHERE `key` = :key")
    fun deleteEntryByKey(key: String)
}
