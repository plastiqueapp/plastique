package io.plastique.deviations.list

import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationActionsState
import javax.inject.Inject

class DeviationItemFactory @Inject constructor(
    private val richTextFormatter: RichTextFormatter
) {
    fun create(deviation: Deviation, index: Int): DeviationItem {
        val actionsState = DeviationActionsState(
            isFavorite = deviation.properties.isFavorite,
            favoriteCount = deviation.stats.favorites,
            isCommentsEnabled = deviation.properties.allowsComments,
            commentCount = deviation.stats.comments)

        return when (val data = deviation.data) {
            is Deviation.Data.Image ->
                ImageDeviationItem(
                    deviationId = deviation.id,
                    title = deviation.title,
                    actionsState = actionsState,
                    content = data.content,
                    preview = data.preview,
                    thumbnails = data.thumbnails,
                    index = index)

            is Deviation.Data.Literature ->
                LiteratureDeviationItem(
                    deviationId = deviation.id,
                    title = deviation.title,
                    actionsState = actionsState,
                    excerpt = SpannedWrapper(richTextFormatter.format(data.excerpt)),
                    index = index)

            is Deviation.Data.Video -> TODO()
        }
    }
}
