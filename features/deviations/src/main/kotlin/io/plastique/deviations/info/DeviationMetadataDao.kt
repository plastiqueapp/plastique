package io.plastique.deviations.info

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import io.plastique.core.converters.StringListConverter
import io.reactivex.Observable

@Dao
interface DeviationMetadataDao {
    @Transaction
    @TypeConverters(StringListConverter::class)
    @Query("""SELECT d.title, d.author_id, d.publish_time, dm.description, dm.tags FROM deviations d
INNER JOIN deviation_metadata dm ON d.id = dm.deviation_id
WHERE d.id = :deviationId""")
    fun getDeviationInfoById(deviationId: String): Observable<List<DeviationInfoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(metadata: Collection<DeviationMetadataEntity>)

    @Update
    fun update(metadata: Collection<DeviationMetadataEntity>)

    @Transaction
    fun insertOrUpdate(metadata: Collection<DeviationMetadataEntity>) {
        update(metadata)
        insert(metadata)
    }
}
