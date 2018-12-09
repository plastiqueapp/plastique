package io.plastique.statuses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Observable

@Dao
interface StatusDao {
    @Transaction
    @Query("SELECT * FROM statuses WHERE id = :statusId")
    fun getStatusById(statusId: String): Observable<List<StatusEntityWithRelations>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(statuses: Collection<StatusEntity>)

    @Update
    fun update(statuses: Collection<StatusEntity>)

    @Transaction
    fun insertOrUpdate(statuses: Collection<StatusEntity>) {
        update(statuses)
        insert(statuses)
    }
}
