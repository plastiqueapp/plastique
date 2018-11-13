package io.plastique.deviations

import io.plastique.images.Image
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class Deviation(
    val id: String,
    val title: String,
    val url: String,
    val content: Image?,
    val preview: Image?,
    val excerpt: String?,
    val author: User,
    val properties: Properties,
    val dailyDeviation: DailyDeviation?
) {
    val isLiterature: Boolean
        get() = excerpt != null

    data class Properties(
        val isDownloadable: Boolean,
        val isFavorite: Boolean,
        val isMature: Boolean
    )
}

data class DailyDeviation(
    val body: String,
    val date: ZonedDateTime,
    val giver: User
)
