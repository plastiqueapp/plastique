package io.plastique.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM USERS WHERE name = :username")
    fun getUserByName(username: String): Maybe<UserEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: UserEntity): Long

    @Update
    fun update(user: UserEntity)

    @Transaction
    fun insertOrUpdate(user: UserEntity) {
        if (insert(user) == -1L) {
            update(user)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(users: Collection<UserEntity>)

    @Update
    fun update(users: Collection<UserEntity>)

    @Transaction
    fun insertOrUpdate(users: Collection<UserEntity>) {
        update(users)
        insert(users)
    }

    @Transaction
    @Query("SELECT up.* FROM user_profiles up INNER JOIN users u ON up.user_id = u.id WHERE u.name = :username")
    fun getProfileByName(username: String): Flowable<UserProfileWithUser>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(userProfile: UserProfileEntity): Long

    @Update
    fun update(userProfile: UserProfileEntity)

    @Transaction
    fun insertOrUpdate(userProfile: UserProfileEntity) {
        if (insert(userProfile) == -1L) {
            update(userProfile)
        }
    }

    @Query("DELETE FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE name = :username)")
    fun deleteProfileByName(username: String)
}
