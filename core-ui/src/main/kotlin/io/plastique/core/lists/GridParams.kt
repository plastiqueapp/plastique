package io.plastique.core.lists

import io.plastique.util.Size
import kotlin.math.roundToInt

data class GridParams(
    val columnCount: Int,
    private val itemSize: Size,
    private val excessSpace: Int
) {
    fun getItemSize(index: Int): Size {
        return if (columnCount - (index % columnCount) - 1 < excessSpace) {
            itemSize.copy(width = itemSize.width + 1)
        } else {
            itemSize
        }
    }
}

object GridParamsCalculator {
    fun calculateGridParams(width: Int, minItemWidth: Int, itemSpacing: Int, heightToWidthRatio: Float = 1.0f): GridParams {
        val columnCount = (width + itemSpacing) / (minItemWidth + itemSpacing)
        val itemWidth = (width - (columnCount - 1) * itemSpacing) / columnCount
        val itemHeight = (itemWidth * heightToWidthRatio).roundToInt()
        val excessWidth = width - columnCount * itemWidth - (columnCount - 1) * itemSpacing
        return GridParams(
                columnCount = columnCount,
                itemSize = Size.of(itemWidth, itemHeight),
                excessSpace = excessWidth)
    }
}
