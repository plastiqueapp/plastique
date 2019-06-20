package io.plastique.deviations.list

import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ListItem
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationActionsState

sealed class DeviationItem : ListItem, IndexedItem {
    abstract val deviationId: String
    abstract val title: String
    abstract val actionsState: DeviationActionsState

    override val id: String get() = deviationId
}

data class ImageDeviationItem(
    override val deviationId: String,
    override val title: String,
    override val actionsState: DeviationActionsState,
    override val index: Int,
    val content: Deviation.ImageInfo?,
    val preview: Deviation.ImageInfo,
    val thumbnails: List<Deviation.ImageInfo>
) : DeviationItem()

data class LiteratureDeviationItem(
    override val deviationId: String,
    override val title: String,
    override val actionsState: DeviationActionsState,
    override val index: Int,
    val excerpt: SpannedWrapper
) : DeviationItem()
