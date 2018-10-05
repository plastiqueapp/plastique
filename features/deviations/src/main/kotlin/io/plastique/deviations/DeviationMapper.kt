package io.plastique.deviations

import io.plastique.api.deviations.DailyDeviation
import io.plastique.api.deviations.Deviation
import io.plastique.images.ImageMapper
import javax.inject.Inject

class DeviationMapper @Inject constructor(
    private val imageMapper: ImageMapper
) {
    fun map(deviation: Deviation): DeviationEntity {
        return DeviationEntity(
                id = deviation.id,
                title = deviation.title,
                url = deviation.url,
                isDownloadable = deviation.isDownloadable,
                isFavorite = deviation.isFavorite,
                isMature = deviation.isMature,
                content = deviation.content?.let { imageMapper.map(it) },
                preview = deviation.preview?.let { imageMapper.map(it) },
                excerpt = deviation.excerpt,
                dailyDeviation = deviation.dailyDeviation?.let(::map),
                authorId = deviation.author.id)
    }

    private fun map(dailyDeviation: DailyDeviation): DailyDeviationEntity {
        return DailyDeviationEntity(
                body = dailyDeviation.body,
                date = dailyDeviation.date,
                giverId = dailyDeviation.giver.id)
    }
}
