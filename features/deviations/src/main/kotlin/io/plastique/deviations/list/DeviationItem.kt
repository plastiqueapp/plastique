package io.plastique.deviations.list

import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ListItem
import io.plastique.deviations.Deviation

data class DeviationItem(val deviation: Deviation) : ListItem, IndexedItem {
    override val id: String get() = deviation.id

    override var index: Int = 0
}
