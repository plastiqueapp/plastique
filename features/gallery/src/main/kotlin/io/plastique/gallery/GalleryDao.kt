package io.plastique.gallery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface GalleryDao {
    @Query("SELECT gf.* FROM gallery_folders gf INNER JOIN user_gallery_folders ugf ON gf.id = ugf.folder_id WHERE ugf.`key` = :key ORDER BY ugf.`order`")
    fun getFolders(key: String): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFolders(folders: List<FolderEntity>)

    @Update
    fun updateFolders(folders: List<FolderEntity>)

    @Transaction
    fun insertOrUpdateFolders(folders: List<FolderEntity>) {
        updateFolders(folders)
        insertFolders(folders)
    }

    @Query("SELECT max(`order`) FROM user_gallery_folders WHERE `key` = :key")
    fun maxOrder(key: String): Int

    @Query("DELETE FROM gallery_folders WHERE id IN (SELECT folder_id FROM user_gallery_folders WHERE `key` = :key)")
    fun deleteFoldersByKey(key: String)

    @Insert
    fun insertLinks(links: List<FolderLinkage>)
}
