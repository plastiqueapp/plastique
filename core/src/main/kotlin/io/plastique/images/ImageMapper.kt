package io.plastique.images

import io.plastique.api.common.ImageInfo
import javax.inject.Inject

class ImageMapper @Inject constructor() {
    fun map(imageInfo: ImageInfo): ImageEntity {
        return ImageEntity(
                width = imageInfo.width,
                height = imageInfo.height,
                url = imageInfo.url)
    }
}
