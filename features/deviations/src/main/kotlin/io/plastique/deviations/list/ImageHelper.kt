package io.plastique.deviations.list

import android.view.View
import io.plastique.deviations.Deviation
import io.plastique.util.Size
import kotlin.math.min

object ImageHelper {
    private const val MAX_IMAGE_WIDTH = 1080
    private const val MAX_ASPECT_RATIO = 2.5 // height / width

    fun getMaxWidth(parent: View): Int {
        val parentWidth = parent.width
        require(parentWidth > 0) { "Parent has $parentWidth width" }
        return min(parentWidth, MAX_IMAGE_WIDTH)
    }

    fun chooseThumbnail(thumbnails: Collection<Deviation.ImageInfo>, itemWidth: Int): Deviation.ImageInfo {
        return thumbnails.firstOrNull { it.size.width >= itemWidth } ?: thumbnails.last()
    }

    fun choosePreview(preview: Deviation.ImageInfo, content: Deviation.ImageInfo?, maxImageWidth: Int): Deviation.ImageInfo {
        return if (content == null || preview.size.width >= maxImageWidth) preview else content
    }

    fun calculateOptimalPreviewSize(imageInfo: Deviation.ImageInfo, maxImageWidth: Int): Size {
        var imageWidth = imageInfo.size.width
        var imageHeight = imageInfo.size.height
        var aspectRatio = imageHeight / imageWidth.toDouble()
        if (aspectRatio > MAX_ASPECT_RATIO || imageWidth > maxImageWidth) {
            aspectRatio = min(aspectRatio, MAX_ASPECT_RATIO)
            imageWidth = min(imageWidth, maxImageWidth)
            imageHeight = (imageWidth * aspectRatio).toInt()
            return Size(imageWidth, imageHeight)
        }
        return imageInfo.size
    }
}
