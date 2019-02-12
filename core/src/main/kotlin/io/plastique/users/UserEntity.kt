package io.plastique.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.api.users.UserDto

@Entity(tableName = "users",
        indices = [
            Index("name")
        ])
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String
)
