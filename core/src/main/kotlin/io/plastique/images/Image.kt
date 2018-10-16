package io.plastique.images

import io.plastique.util.Size

data class Image(
    val size: Size,
    val url: String
)

fun ImageEntity.toImage(): Image = Image(size = size, url = url)
