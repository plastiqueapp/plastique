package io.plastique.deviations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Observable

@Dao
interface DeviationDao {
    @Transaction
    @Query("SELECT * FROM deviations WHERE id = :deviationId")
    fun getDeviationById(deviationId: String): Observable<List<DeviationEntityWithRelations>>

    @Transaction
    @Query("SELECT d.* FROM deviations d INNER JOIN deviation_linkage dl ON d.id = dl.deviation_id WHERE dl.`key` = :key ORDER BY dl.`order`")
    fun getDeviationsByKey(key: String): List<DeviationEntityWithRelations>

    @Query("SELECT title FROM deviations WHERE id = :deviationId")
    fun getDeviationTitleById(deviationId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(deviation: DeviationEntity): Long

    @Update
    fun update(deviation: DeviationEntity)

    @Transaction
    fun insertOrUpdate(deviation: DeviationEntity) {
        if (insert(deviation) == -1L) {
            update(deviation)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(metadata: Collection<DeviationEntity>)

    @Update
    fun update(metadata: Collection<DeviationEntity>)

    @Transaction
    fun insertOrUpdate(metadata: Collection<DeviationEntity>) {
        update(metadata)
        insert(metadata)
    }

    @Insert
    fun insertLinks(links: Collection<DeviationLinkage>)

    @Query("DELETE FROM deviation_linkage WHERE `key` = :key")
    fun deleteLinksByKey(key: String)

    @Query("SELECT coalesce(max(`order`), 0) FROM deviation_linkage WHERE `key` = :key")
    fun getMaxOrder(key: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertImages(images: Collection<DeviationImageEntity>)

    @Update
    fun updateImages(images: Collection<DeviationImageEntity>)

    @Query("DELETE FROM deviation_images WHERE deviation_id IN (:deviationIds) AND id NOT IN (:exceptIds)")
    fun deleteImages(exceptIds: Set<String>, deviationIds: Set<String>)

    @Transaction
    fun replaceImages(images: Collection<DeviationImageEntity>) {
        val ids = images.mapTo(mutableSetOf()) { it.id }
        val deviationIds = images.mapTo(mutableSetOf()) { it.deviationId }
        deleteImages(deviationIds = deviationIds, exceptIds = ids)
        updateImages(images)
        insertImages(images)
    }
}
