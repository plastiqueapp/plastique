package io.plastique.statuses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface StatusDao {
    @Transaction
    @Query("SELECT statuses.* FROM statuses INNER JOIN user_statuses ON statuses.id = user_statuses.status_id WHERE user_statuses.`key` = :key ORDER BY user_statuses.`order`")
    fun getStatusesByKey(key: String): List<StatusEntityWithRelations>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(statuses: Collection<StatusEntity>)

    @Update
    fun update(statuses: Collection<StatusEntity>)

    @Transaction
    fun insertOrUpdate(statuses: Collection<StatusEntity>) {
        update(statuses)
        insert(statuses)
    }

    @Insert
    fun insertLinks(links: Collection<StatusLinkage>)

    @Query("DELETE FROM user_statuses WHERE `key` = :key")
    fun deleteLinksByKey(key: String)

    @Query("SELECT coalesce(max(`order`), 0) FROM user_statuses WHERE `key` = :key")
    fun getMaxOrder(key: String): Int
}
