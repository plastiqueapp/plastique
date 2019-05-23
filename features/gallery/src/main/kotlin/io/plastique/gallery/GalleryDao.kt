package io.plastique.gallery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface GalleryDao {
    @Query("""SELECT gallery_folders.* FROM gallery_folders
INNER JOIN user_gallery_folders ON gallery_folders.id = user_gallery_folders.folder_id
WHERE user_gallery_folders.`key` = :key
  AND user_gallery_folders.folder_id NOT IN (SELECT folder_id FROM deleted_gallery_folders)
ORDER BY user_gallery_folders.`order`""")
    fun getFolders(key: String): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFolder(folder: FolderEntity)

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

    @Query("DELETE FROM gallery_folders WHERE id = :folderId")
    fun deleteFolderById(folderId: String)

    @Query("DELETE FROM gallery_folders WHERE id IN (SELECT folder_id FROM user_gallery_folders WHERE `key` = :key)")
    fun deleteFoldersByKey(key: String)

    @Insert
    fun insertLink(link: FolderLinkage)

    @Insert
    fun insertLinks(links: Collection<FolderLinkage>)

    @Query("SELECT folder_id FROM deleted_gallery_folders ORDER BY rowid")
    fun getDeletedFolderIds(): Single<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDeletedFolder(deletedFolder: DeletedFolderEntity)

    @Query("DELETE FROM deleted_gallery_folders WHERE folder_id = :folderId")
    fun removeDeletedFolder(folderId: String)

    @Transaction
    @Query("DELETE FROM deleted_gallery_folders")
    fun removeDeletedFolders(): Completable
}
