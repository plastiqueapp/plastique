package io.plastique.deviations.list

import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ListItem
import io.plastique.deviations.Deviation

abstract class DeviationItem : ListItem, IndexedItem {
    abstract val deviation: Deviation

    override val id: String get() = deviation.id
}

data class ImageDeviationItem(
    override val deviation: Deviation,
    override var index: Int = 0
) : DeviationItem()

data class LiteratureDeviationItem(
    override val deviation: Deviation,
    override var index: Int = 0,
    val excerpt: CharSequence
) : DeviationItem()
