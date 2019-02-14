package io.plastique.notifications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface MessageDao {
    @Transaction
    @Query("SELECT * FROM messages ORDER BY time DESC")
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
}
