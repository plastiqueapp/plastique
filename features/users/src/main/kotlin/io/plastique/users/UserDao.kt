package io.plastique.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.plastique.users.profile.UserProfileEntity
import io.plastique.users.profile.UserProfileEntityWithRelations
import io.reactivex.Observable

@Dao
interface UserDao {
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
    fun getProfileByName(username: String): Observable<UserProfileEntityWithRelations>

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

    @Transaction
    @Query("UPDATE user_profiles SET is_watching = :watching WHERE user_id IN (SELECT id FROM users WHERE name = :username)")
    fun setWatching(username: String, watching: Boolean)
}
