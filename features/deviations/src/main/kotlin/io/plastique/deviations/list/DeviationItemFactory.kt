package io.plastique.deviations.list

import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import javax.inject.Inject

class DeviationItemFactory @Inject constructor(
    private val richTextFormatter: RichTextFormatter
) {
    fun create(deviation: Deviation, index: Int): DeviationItem {
        return when (val data = deviation.data) {
            is Deviation.Data.Image ->
                ImageDeviationItem(
                    deviationId = deviation.id,
                    title = deviation.title,
                    isFavorite = deviation.properties.isFavorite,
                    allowsComments = deviation.properties.allowsComments,
                    favoriteCount = deviation.stats.favorites,
                    commentCount = deviation.stats.comments,
                    content = data.content,
                    preview = data.preview,
                    thumbnails = data.thumbnails,
                    index = index)

            is Deviation.Data.Literature ->
                LiteratureDeviationItem(
                    deviationId = deviation.id,
                    title = deviation.title,
                    isFavorite = deviation.properties.isFavorite,
                    allowsComments = deviation.properties.allowsComments,
                    favoriteCount = deviation.stats.favorites,
                    commentCount = deviation.stats.comments,
                    excerpt = SpannedWrapper(richTextFormatter.format(data.excerpt)),
                    index = index)

            is Deviation.Data.Video -> TODO()
        }
    }
}
