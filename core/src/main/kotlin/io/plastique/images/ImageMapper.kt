package io.plastique.images

import io.plastique.api.common.ImageInfo
import io.plastique.util.Size
import javax.inject.Inject

class ImageMapper @Inject constructor() {
    fun map(imageInfo: ImageInfo): ImageEntity {
        return ImageEntity(size = Size.of(imageInfo.width, imageInfo.height), url = imageInfo.url)
    }
}
