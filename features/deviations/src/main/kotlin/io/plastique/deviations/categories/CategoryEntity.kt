package io.plastique.deviations.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index("parent")
    ])
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "path")
    val path: String,

    @ColumnInfo(name = "parent")
    val parent: String?,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "has_children")
    val hasChildren: Boolean
)
