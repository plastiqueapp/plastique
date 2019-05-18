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
    @Query("""SELECT deviations.title, deviations.author_id, deviations.publish_time, deviation_metadata.description, deviation_metadata.tags FROM deviations
INNER JOIN deviation_metadata ON deviations.id = deviation_metadata.deviation_id
WHERE deviations.id = :deviationId""")
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
