package io.plastique.deviations

import io.plastique.users.User
import io.plastique.util.Size
import org.threeten.bp.ZonedDateTime

data class Deviation(
    val id: String,
    val title: String,
    val url: String,
    val author: User,
    val properties: Properties,
    val stats: Stats,
    val dailyDeviation: DailyDeviation?,

    val content: Image?,
    val preview: Image?,
    val thumbnails: List<Image>,

    val excerpt: String?
) {
    val isLiterature: Boolean
        get() = excerpt != null

    data class DailyDeviation(
        val body: String,
        val date: ZonedDateTime,
        val giver: User
    )

    data class Properties(
        val isDownloadable: Boolean,
        val isFavorite: Boolean,
        val isMature: Boolean,
        val allowsComments: Boolean
    )

    data class Stats(
        val comments: Int,
        val favorites: Int
    )

    data class Image(
        val size: Size,
        val url: String
    )
}
