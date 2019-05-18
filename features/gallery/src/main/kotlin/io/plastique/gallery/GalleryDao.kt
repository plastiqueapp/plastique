package io.plastique.gallery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface GalleryDao {
    @Query("""SELECT gallery_folders.* FROM gallery_folders
INNER JOIN user_gallery_folders ON gallery_folders.id = user_gallery_folders.folder_id
WHERE user_gallery_folders.`key` = :key
ORDER BY user_gallery_folders.`order`""")
    fun getFolders(key: String): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFolders(folders: Collection<FolderEntity>)

    @Update
    fun updateFolders(folders: Collection<FolderEntity>)

    @Transaction
    fun insertOrUpdateFolders(folders: Collection<FolderEntity>) {
        updateFolders(folders)
        insertFolders(folders)
    }

    @Query("SELECT max(`order`) FROM user_gallery_folders WHERE `key` = :key")
    fun maxOrder(key: String): Int

    @Query("DELETE FROM gallery_folders WHERE id IN (SELECT folder_id FROM user_gallery_folders WHERE `key` = :key)")
    fun deleteFoldersByKey(key: String)

    @Insert
    fun insertLinks(links: Collection<FolderLinkage>)
}
