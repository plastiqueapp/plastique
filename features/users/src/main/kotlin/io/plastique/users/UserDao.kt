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
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Observable<List<UserEntity>>

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
    @Query("""SELECT user_profiles.* FROM user_profiles
INNER JOIN users ON user_profiles.user_id = users.id
WHERE users.name = :username""")
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

    @Query("SELECT is_watching, stats_watchers FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE name = :username)")
    fun getWatchInfo(username: String): WatchInfoEntity?

    @Query("UPDATE user_profiles SET is_watching = :watching, stats_watchers = :watcherCount WHERE user_id IN (SELECT id FROM users WHERE name = :username)")
    fun setWatching(username: String, watching: Boolean, watcherCount: Int)
}
