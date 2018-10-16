package io.plastique.images

import androidx.room.ColumnInfo
import io.plastique.api.common.ImageInfo
import io.plastique.util.Size

data class ImageEntity(
    @ColumnInfo(name = "size")
    val size: Size,

    @ColumnInfo(name = "url")
    val url: String
)

fun ImageInfo.toImageEntity(): ImageEntity = ImageEntity(size = Size.of(width, height), url = url)
