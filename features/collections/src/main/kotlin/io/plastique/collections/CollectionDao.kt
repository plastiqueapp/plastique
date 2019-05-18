package io.plastique.collections

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface CollectionDao {
    @Query("""SELECT collection_folders.* FROM collection_folders
INNER JOIN user_collection_folders ON collection_folders.id = user_collection_folders.folder_id
WHERE user_collection_folders.`key` = :key
ORDER BY user_collection_folders.`order`""")
    fun getFoldersByKey(key: String): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFolders(folders: Collection<FolderEntity>)

    @Update
    fun updateFolders(folders: Collection<FolderEntity>)

    @Transaction
    fun insertOrUpdateFolders(folders: Collection<FolderEntity>) {
        updateFolders(folders)
        insertFolders(folders)
    }

    @Query("SELECT max(`order`) FROM user_collection_folders WHERE `key` = :key")
    fun maxOrder(key: String): Int

    @Query("DELETE FROM collection_folders WHERE id IN (SELECT folder_id FROM user_collection_folders WHERE `key` = :key)")
    fun deleteFoldersByKey(key: String)

    @Insert
    fun insertLinks(links: Collection<FolderLinkage>)
}
