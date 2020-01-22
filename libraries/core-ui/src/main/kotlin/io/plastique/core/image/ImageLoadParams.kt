package io.plastique.core.image

import androidx.annotation.DrawableRes
import io.plastique.util.Size

class ImageLoadParams internal constructor() {
    @DrawableRes var errorDrawable: Int = 0
    @DrawableRes var fallbackDrawable: Int = 0
    @DrawableRes var placeholderDrawable: Int = 0

    var animate: Boolean = false
    var cacheSource: Boolean = false
    var cacheInMemory: Boolean = true
    var size: Size? = null
    var thumbnailUrls: List<String>? = null
    val transforms: MutableList<TransformType> = mutableListOf()
}

enum class TransformType {
    CenterCrop,
    CircleCrop
}
