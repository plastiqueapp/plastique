package io.plastique.deviations

import io.plastique.users.User
import io.plastique.util.Size
import org.threeten.bp.ZonedDateTime

data class Deviation(
    val id: String,
    val title: String,
    val url: String,
    val categoryPath: String,
    val publishTime: ZonedDateTime,
    val author: User,
    val data: Data,
    val properties: Properties,
    val stats: Stats,
    val dailyDeviation: DailyDeviation?
) {
    sealed class Data {
        data class Image(
            val content: ImageInfo,
            val preview: ImageInfo,
            val thumbnails: List<ImageInfo>
        ) : Data()

        data class Literature(
            val excerpt: String
        ) : Data()

        data class Video(
            val thumbnails: List<ImageInfo>,
            val preview: ImageInfo,
            val videos: List<VideoInfo>
        ) : Data()
    }

    data class DailyDeviation(
        val body: String,
        val date: ZonedDateTime,
        val giver: User
    )

    data class Properties(
        val isDownloadable: Boolean,
        val isFavorite: Boolean,
        val isMature: Boolean,
        val allowsComments: Boolean,
        val downloadFileSize: Long
    )

    data class Stats(
        val comments: Int,
        val favorites: Int
    )

    data class ImageInfo(
        val size: Size,
        val url: String
    )

    data class VideoInfo(
        val quality: String,
        val url: String
    )
}
