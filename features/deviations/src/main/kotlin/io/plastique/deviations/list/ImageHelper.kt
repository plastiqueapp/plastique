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

    fun chooseThumbnail(deviation: Deviation, itemWidth: Int): Deviation.Image {
        return deviation.thumbnails.firstOrNull { it.size.width >= itemWidth } ?: deviation.thumbnails.last()
    }

    fun choosePreview(deviation: Deviation, maxImageWidth: Int): Deviation.Image {
        val content = deviation.content
        val preview = deviation.preview
        return if (preview != null && (content == null || preview.size.width >= maxImageWidth)) preview else content
            ?: throw IllegalStateException("No preview available")
    }

    fun calculateOptimalPreviewSize(image: Deviation.Image, maxImageWidth: Int): Size {
        var imageWidth = image.size.width
        var imageHeight = image.size.height
        var aspectRatio = imageHeight / imageWidth.toDouble()
        if (aspectRatio > MAX_ASPECT_RATIO || imageWidth > maxImageWidth) {
            aspectRatio = min(aspectRatio, MAX_ASPECT_RATIO)
            imageWidth = min(imageWidth, maxImageWidth)
            imageHeight = (imageWidth * aspectRatio).toInt()
            return Size(imageWidth, imageHeight)
        }
        return image.size
    }
}
