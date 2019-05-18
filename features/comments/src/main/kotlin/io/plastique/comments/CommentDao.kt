package io.plastique.comments

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface CommentDao {
    @Transaction
    @Query("""SELECT comments.* FROM comments
INNER JOIN comment_linkage ON comments.id = comment_linkage.comment_id
WHERE comment_linkage.`key` = :key
ORDER BY comment_linkage.`order`""")
    fun getCommentsByKey(key: String): List<CommentEntityWithRelations>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(comments: Collection<CommentEntity>)

    @Update
    fun update(comments: Collection<CommentEntity>)

    @Transaction
    fun insertOrUpdate(comments: Collection<CommentEntity>) {
        update(comments)
        insert(comments)
    }

    @Insert
    fun insertLinks(linkage: Collection<CommentLinkage>)

    @Query("DELETE FROM comment_linkage WHERE `key` = :key")
    fun deleteLinks(key: String)

    @Query("SELECT coalesce(max(`order`), 0) FROM comment_linkage WHERE `key` = :key")
    fun maxOrder(key: String): Int
}
