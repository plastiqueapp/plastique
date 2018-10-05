package io.plastique.deviations.categories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Single

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE parent = :path")
    fun getSubcategories(path: String): Single<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(categories: List<CategoryEntity>)

    @Update
    fun update(categories: List<CategoryEntity>)

    @Transaction
    fun insertOrUpdate(categories: List<CategoryEntity>) {
        update(categories)
        insert(categories)
    }
}
