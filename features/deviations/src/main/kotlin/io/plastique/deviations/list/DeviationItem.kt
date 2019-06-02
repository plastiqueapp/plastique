package io.plastique.deviations.list

import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ListItem
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import javax.inject.Inject

abstract class DeviationItem : ListItem, IndexedItem {
    abstract val deviation: Deviation

    override val id: String get() = deviation.id
}

data class ImageDeviationItem(
    override val deviation: Deviation,
    override val index: Int
) : DeviationItem()

data class LiteratureDeviationItem(
    override val deviation: Deviation,
    override val index: Int,
    val excerpt: SpannedWrapper
) : DeviationItem()

class DeviationItemFactory @Inject constructor(
    private val richTextFormatter: RichTextFormatter
) {
    fun create(deviation: Deviation, index: Int): DeviationItem = when {
        deviation.isLiterature -> LiteratureDeviationItem(
            deviation = deviation,
            excerpt = SpannedWrapper(richTextFormatter.format(deviation.excerpt!!)),
            index = index)

        else -> ImageDeviationItem(
            deviation = deviation,
            index = index)
    }
}
