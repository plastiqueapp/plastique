package io.plastique.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import io.plastique.api.users.UserProfileDto

@Entity(tableName = "user_profiles",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"])
        ])
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "profile_url")
    val profileUrl: String,

    @ColumnInfo(name = "real_name")
    val realName: String?
)

fun UserProfileDto.toUserProfileEntity(): UserProfileEntity =
        UserProfileEntity(userId = user.id, profileUrl = profileUrl, realName = realName)
