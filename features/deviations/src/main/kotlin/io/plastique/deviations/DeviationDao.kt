package io.plastique.deviations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Maybe
import io.reactivex.Observable

@Dao
interface DeviationDao {
    @Transaction
    @Query("SELECT * FROM deviations WHERE id = :deviationId")
    fun getDeviationById(deviationId: String): Observable<List<DeviationEntityWithRelations>>

    @Transaction
    @Query("""SELECT deviations.* FROM deviations
INNER JOIN deviation_linkage ON deviations.id = deviation_linkage.deviation_id
WHERE deviation_linkage.`key` = :key
ORDER BY deviation_linkage.`order`""")
    fun getDeviationsByKey(key: String): List<DeviationEntityWithRelations>

    @Query("SELECT title FROM deviations WHERE id = :deviationId")
    fun getDeviationTitleById(deviationId: String): Maybe<String>

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
    fun insert(deviations: Collection<DeviationEntity>)

    @Update
    fun update(deviations: Collection<DeviationEntity>)

    @Transaction
    fun insertOrUpdate(deviations: Collection<DeviationEntity>) {
        update(deviations)
        insert(deviations)
    }

    @Query("UPDATE deviations SET properties_is_favorite = :favorite WHERE id = :deviationId")
    fun setFavorite(deviationId: String, favorite: Boolean)

    @Query("UPDATE deviations SET properties_is_favorite = :favorite, stats_favorites = :numFavorites WHERE id = :deviationId")
    fun setFavorite(deviationId: String, favorite: Boolean, numFavorites: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLinks(links: Collection<DeviationLinkage>)

    @Query("DELETE FROM deviation_linkage WHERE `key` = :key")
    fun deleteLinksByKey(key: String)

    @Query("SELECT coalesce(max(`order`), 0) FROM deviation_linkage WHERE `key` = :key")
    fun getMaxOrder(key: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDailyDeviations(dailyDeviations: Collection<DailyDeviationEntity>)

    @Update
    fun updateDailyDeviations(dailyDeviations: Collection<DailyDeviationEntity>)

    @Transaction
    fun insertOrUpdateDailyDeviations(dailyDeviations: Collection<DailyDeviationEntity>) {
        updateDailyDeviations(dailyDeviations)
        insertDailyDeviations(dailyDeviations)
    }

    @Query("DELETE FROM daily_deviations WHERE deviation_id IN (:deviationIds)")
    fun deleteDailyDeviations(deviationIds: Collection<String>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertImages(images: Collection<DeviationImageEntity>)

    @Update
    fun updateImages(images: Collection<DeviationImageEntity>)

    @Query("DELETE FROM deviation_images WHERE deviation_id IN (:deviationIds) AND id NOT IN (:exceptIds)")
    fun deleteImages(deviationIds: Collection<String>, exceptIds: Collection<String>)

    @Transaction
    fun replaceImages(images: Collection<DeviationImageEntity>) {
        val ids = images.mapTo(mutableSetOf()) { it.id }
        val deviationIds = images.mapTo(mutableSetOf()) { it.deviationId }
        deleteImages(deviationIds, ids)
        updateImages(images)
        insertImages(images)
    }

    @Insert
    fun insertVideos(videos: Collection<DeviationVideoEntity>)

    @Query("DELETE FROM deviation_videos WHERE deviation_id IN (:deviationIds)")
    fun deleteVideos(deviationIds: Collection<String>)

    @Transaction
    fun replaceVideos(videos: Collection<DeviationVideoEntity>) {
        val deviationIds = videos.mapTo(mutableSetOf()) { it.deviationId }
        deleteVideos(deviationIds)
        insertVideos(videos)
    }
}
