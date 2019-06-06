package io.plastique.deviations.list

import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation

abstract class DeviationItem : ListItem, IndexedItem {
    abstract val deviationId: String
    abstract val title: String
    abstract val isFavorite: Boolean
    abstract val allowsComments: Boolean
    abstract val favoriteCount: Int
    abstract val commentCount: Int

    override val id: String get() = deviationId
}

data class ImageDeviationItem(
    override val deviationId: String,
    override val title: String,
    override val isFavorite: Boolean,
    override val allowsComments: Boolean,
    override val favoriteCount: Int,
    override val commentCount: Int,
    override val index: Int,
    val content: Deviation.ImageInfo?,
    val preview: Deviation.ImageInfo,
    val thumbnails: List<Deviation.ImageInfo>
) : DeviationItem()

data class LiteratureDeviationItem(
    override val deviationId: String,
    override val title: String,
    override val isFavorite: Boolean,
    override val allowsComments: Boolean,
    override val favoriteCount: Int,
    override val commentCount: Int,
    override val index: Int,
    val excerpt: SpannedWrapper
) : DeviationItem()
