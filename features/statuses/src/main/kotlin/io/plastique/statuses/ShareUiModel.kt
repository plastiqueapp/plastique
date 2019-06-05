package io.plastique.statuses

import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

sealed class ShareUiModel {
    object None : ShareUiModel()

    data class ImageDeviation(
        val deviationId: String,
        val author: User,
        val title: String,
        val preview: Deviation.ImageInfo,
        val isConcealedMature: Boolean
    ) : ShareUiModel()

    data class LiteratureDeviation(
        val deviationId: String,
        val author: User,
        val date: ZonedDateTime,
        val title: String,
        val excerpt: SpannedWrapper,
        val isJournal: Boolean
    ) : ShareUiModel()

    data class Status(
        val statusId: String,
        val author: User,
        val date: ZonedDateTime,
        val text: SpannedWrapper
    ) : ShareUiModel()

    object DeletedDeviation : ShareUiModel()
    object DeletedStatus : ShareUiModel()
}

val ShareUiModel.isDeleted: Boolean
    get() = this === ShareUiModel.DeletedDeviation || this === ShareUiModel.DeletedStatus

fun Status.Share.toShareUiModel(richTextFormatter: RichTextFormatter, matureContent: Boolean): ShareUiModel = when (this) {
    Status.Share.None -> ShareUiModel.None

    is Status.Share.DeviationShare -> {
        val deviation = deviation
        when {
            deviation == null -> ShareUiModel.DeletedDeviation
            deviation.isLiterature -> {
                ShareUiModel.LiteratureDeviation(
                    deviationId = deviation.id,
                    date = deviation.publishTime,
                    author = deviation.author,
                    title = deviation.title,
                    excerpt = SpannedWrapper(richTextFormatter.format(deviation.excerpt!!)),
                    isJournal = deviation.categoryPath.startsWith("journals"))
            }
            else -> ShareUiModel.ImageDeviation(
                deviationId = deviation.id,
                author = deviation.author,
                title = deviation.title,
                preview = deviation.preview!!,
                isConcealedMature = deviation.properties.isMature && !matureContent)
        }
    }

    is Status.Share.StatusShare -> {
        val status = status
        if (status != null) {
            ShareUiModel.Status(
                statusId = status.id,
                author = status.author,
                date = status.date,
                text = SpannedWrapper(richTextFormatter.format(status.body)))
        } else {
            ShareUiModel.DeletedStatus
        }
    }
}
