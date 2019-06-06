package io.plastique.deviations.list

import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import javax.inject.Inject

class DeviationItemFactory @Inject constructor(
    private val richTextFormatter: RichTextFormatter
) {
    fun create(deviation: Deviation, index: Int): DeviationItem {
        return when {
            deviation.isLiterature ->
                LiteratureDeviationItem(
                    deviationId = deviation.id,
                    title = deviation.title,
                    isFavorite = deviation.properties.isFavorite,
                    allowsComments = deviation.properties.allowsComments,
                    favoriteCount = deviation.stats.favorites,
                    commentCount = deviation.stats.comments,
                    excerpt = SpannedWrapper(richTextFormatter.format(deviation.excerpt!!)),
                    index = index)

            else ->
                ImageDeviationItem(
                    deviationId = deviation.id,
                    title = deviation.title,
                    isFavorite = deviation.properties.isFavorite,
                    allowsComments = deviation.properties.allowsComments,
                    favoriteCount = deviation.stats.favorites,
                    commentCount = deviation.stats.comments,
                    content = deviation.content,
                    preview = deviation.preview!!,
                    thumbnails = deviation.thumbnails,
                    index = index)
        }
    }
}
