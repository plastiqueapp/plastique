package io.plastique.deviations.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Maybe

@Dao
interface DownloadInfoDao {
    @Query("SELECT * FROM deviation_download WHERE deviation_id = :deviationId")
    fun getDownloadInfo(deviationId: String): Maybe<DownloadInfoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: DownloadInfoEntity): Long

    @Update
    fun update(entity: DownloadInfoEntity)

    @Transaction
    fun insertOrUpdate(entity: DownloadInfoEntity) {
        if (insert(entity) == -1L) {
            update(entity)
        }
    }
}
