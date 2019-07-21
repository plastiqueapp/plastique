package io.plastique.core.lists

import io.plastique.util.Size

interface ItemSizeCallback {
    fun getColumnCount(item: IndexedItem): Int

    fun getItemSize(item: IndexedItem): Size
}

class SimpleGridItemSizeCallback(private val gridParams: GridParams) : ItemSizeCallback {
    override fun getColumnCount(item: IndexedItem): Int = gridParams.columnCount

    override fun getItemSize(item: IndexedItem): Size = gridParams.getItemSize(item.index)
}
