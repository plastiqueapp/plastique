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
    @Query("SELECT c.* FROM comments c INNER JOIN comment_linkage cl ON c.id = cl.comment_id WHERE cl.`key` = :key ORDER BY cl.`order`")
    fun getCommentsWithAuthors(key: String): List<CommentWithAuthor>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(comment: CommentEntity): Long

    @Update
    fun update(comment: CommentEntity)

    @Transaction
    fun insertOrUpdate(comment: CommentEntity) {
        if (insert(comment) == -1L) {
            update(comment)
        }
    }

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
