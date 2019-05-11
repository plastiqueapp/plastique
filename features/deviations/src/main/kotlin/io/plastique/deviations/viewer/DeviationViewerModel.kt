package io.plastique.deviations.viewer

import io.plastique.core.session.SessionManager
import io.plastique.core.session.userId
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationRepository
import io.reactivex.Observable
import javax.inject.Inject

class DeviationViewerModel @Inject constructor(
    private val deviationRepository: DeviationRepository,
    private val sessionManager: SessionManager
) {
    fun getDeviationById(deviationId: String): Observable<DeviationLoadResult> {
        return Observable.combineLatest(
            deviationRepository.getDeviationById(deviationId),
            sessionManager.sessionChanges
                .map { it.userId ?: "" }
                .distinctUntilChanged()
        ) { deviation, userId ->
            val content = deviation.toDeviationContent()

            val infoViewState = InfoViewState(
                title = deviation.title,
                author = deviation.author,
                favoriteCount = deviation.stats.favorites,
                isFavoriteChecked = deviation.properties.isFavorite,
                isFavoriteEnabled = deviation.author.id != userId,
                commentCount = deviation.stats.comments,
                isCommentsEnabled = deviation.properties.allowsComments)

            val menuState = MenuState(
                deviationUrl = deviation.url,
                showDownload = deviation.properties.isDownloadable,
                downloadFileSize = deviation.properties.downloadFileSize)

            DeviationLoadResult(deviationContent = content, infoViewState = infoViewState, menuState = menuState)
        }
    }

    private fun Deviation.toDeviationContent(): DeviationContent = when {
        isLiterature -> DeviationContent.Literature(html = "") // TODO
        else -> {
            val previewUrl = preview!!.url
            DeviationContent.Image(
                url = content?.url ?: previewUrl,
                thumbnailUrls = thumbnails.map { it.url } + previewUrl) // Ordered from lowest to highest resolution
        }
    }
}

data class DeviationLoadResult(
    val deviationContent: DeviationContent,
    val infoViewState: InfoViewState,
    val menuState: MenuState
)
