package io.plastique.images

import androidx.room.ColumnInfo
import io.plastique.api.common.ImageDto
import io.plastique.util.Size

data class ImageEntity(
    @ColumnInfo(name = "size")
    val size: Size,

    @ColumnInfo(name = "url")
    val url: String
)

fun ImageDto.toImageEntity(): ImageEntity = ImageEntity(size = Size(width, height), url = url)
