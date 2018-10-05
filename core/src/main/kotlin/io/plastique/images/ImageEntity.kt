package io.plastique.images

import androidx.room.ColumnInfo

data class ImageEntity(
    @ColumnInfo(name = "width")
    val width: Int,

    @ColumnInfo(name = "height")
    val height: Int,

    @ColumnInfo(name = "url")
    val url: String
)
