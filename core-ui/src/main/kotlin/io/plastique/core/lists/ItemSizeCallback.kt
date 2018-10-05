package io.plastique.core.lists

import io.plastique.util.Size

interface ItemSizeCallback {
    fun getColumnCount(item: IndexedItem): Int

    fun getItemSize(item: IndexedItem): Size
}
