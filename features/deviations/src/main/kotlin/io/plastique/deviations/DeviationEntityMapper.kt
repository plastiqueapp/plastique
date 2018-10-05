package io.plastique.deviations

import io.plastique.images.Image
import io.plastique.images.ImageEntity
import io.plastique.users.UserEntity
import io.plastique.users.UserEntityMapper
import io.plastique.util.Size
import javax.inject.Inject

class DeviationEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
) {
    fun map(entity: DeviationEntity, author: UserEntity, giver: UserEntity?): Deviation {
        if (entity.authorId != author.id) {
            throw IllegalArgumentException("Expected user with id ${entity.authorId} but got ${author.id}")
        }
        if (entity.dailyDeviation?.giverId != giver?.id) {
            throw IllegalArgumentException("Expected user with id ${entity.dailyDeviation?.giverId} but got ${giver?.id}")
        }
        return Deviation(
                id = entity.id,
                title = entity.title,
                url = entity.url,
                content = entity.content?.let(::map),
                preview = entity.preview?.let(::map),
                excerpt = entity.excerpt,
                author = userEntityMapper.map(author),
                properties = Deviation.Properties(
                        isDownloadable = entity.isDownloadable,
                        isFavorite = entity.isFavorite,
                        isMature = entity.isMature),
                dailyDeviation = entity.dailyDeviation?.let { dailyDeviation -> map(dailyDeviation, giver!!) })
    }

    fun map(deviationWithUsers: DeviationWithUsers): Deviation {
        return map(deviationWithUsers.deviation, deviationWithUsers.author.first(), deviationWithUsers.dailyDeviationGiver.firstOrNull())
    }

    private fun map(image: ImageEntity): Image {
        return Image(
                size = Size.of(image.width, image.height),
                url = image.url)
    }

    private fun map(dailyDeviation: DailyDeviationEntity, giver: UserEntity): DailyDeviation {
        if (dailyDeviation.giverId != giver.id) {
            throw IllegalArgumentException("Expected user with id ${dailyDeviation.giverId} but got ${giver.id}")
        }
        return DailyDeviation(
                body = dailyDeviation.body,
                date = dailyDeviation.date,
                giver = userEntityMapper.map(giver))
    }
}
