package io.plastique.deviations

import io.plastique.images.Image
import io.plastique.images.toImage
import io.plastique.users.User
import io.plastique.users.toUser

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

fun DeviationWithUsers.toDeviation(): Deviation = Deviation(
        id = deviation.id,
        title = deviation.title,
        url = deviation.url,
        content = deviation.content?.toImage(),
        preview = deviation.preview?.toImage(),
        excerpt = deviation.excerpt,
        author = author.first().toUser(),
        properties = Deviation.Properties(
                isDownloadable = deviation.isDownloadable,
                isFavorite = deviation.isFavorite,
                isMature = deviation.isMature),
        dailyDeviation = deviation.dailyDeviation?.toDailyDeviation(dailyDeviationGiver.first()))
