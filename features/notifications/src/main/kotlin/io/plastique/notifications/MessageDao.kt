package io.plastique.notifications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Single

@Dao
interface MessageDao {
    @Transaction
    @Query("SELECT * FROM messages WHERE id NOT IN (SELECT message_id FROM deleted_messages) ORDER BY time DESC")
    fun getMessages(): List<MessageEntityWithRelations>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(messages: Collection<MessageEntity>)

    @Update
    fun update(messages: Collection<MessageEntity>)

    @Transaction
    fun insertOrUpdate(messages: Collection<MessageEntity>) {
        update(messages)
        insert(messages)
    }

    @Query("DELETE FROM messages")
    fun deleteAll()

    @Query("DELETE FROM messages WHERE id = :messageId")
    fun deleteMessageById(messageId: String)

    @Query("SELECT message_id FROM deleted_messages")
    fun getDeletedMessageIds(): Single<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDeletedMessage(deletedMessage: DeletedMessageEntity)

    @Query("DELETE FROM deleted_messages WHERE message_id = :messageId")
    fun deleteDeletedMessage(messageId: String)
}
