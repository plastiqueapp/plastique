package io.plastique.deviations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface DeviationMetadataDao {
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
