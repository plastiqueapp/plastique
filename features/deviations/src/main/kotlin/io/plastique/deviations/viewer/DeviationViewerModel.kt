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

    private fun Deviation.toDeviationContent(): DeviationContent {
        return when (val data = data) {
            is Deviation.Data.Image ->
                DeviationContent.Image(
                    url = data.content.url,
                    thumbnailUrls = data.thumbnails.map { it.url } + data.preview.url)

            is Deviation.Data.Literature ->
                DeviationContent.Literature(html = "") // TODO

            is Deviation.Data.Video ->
                DeviationContent.Video(
                    thumbnailUrls = data.thumbnails.map { it.url } + data.preview.url,
                    videos = data.videos)
        }
    }
}

data class DeviationLoadResult(
    val deviationContent: DeviationContent,
    val infoViewState: InfoViewState,
    val menuState: MenuState
)
