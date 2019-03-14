package io.plastique.deviations.viewer

import android.net.Uri
import io.plastique.collections.FavoritesModel
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.R
import io.plastique.deviations.download.DownloadInfoRepository
import io.plastique.deviations.viewer.DeviationViewerEffect.DownloadOriginalEffect
import io.plastique.deviations.viewer.DeviationViewerEffect.LoadDeviationEffect
import io.plastique.deviations.viewer.DeviationViewerEffect.SetFavoriteEffect
import io.plastique.deviations.viewer.DeviationViewerEvent.DeviationLoadedEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalErrorEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.LoadErrorEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SessionChangedEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteErrorEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteFinishedEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.inject.scopes.ActivityScope
import io.plastique.util.FileDownloader
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class DeviationViewerViewModel @Inject constructor(
    stateReducer: DeviationViewerStateReducer,
    private val deviationViewerModel: DeviationViewerModel,
    private val downloadInfoRepository: DownloadInfoRepository,
    private val downloader: FileDownloader,
    private val favoritesModel: FavoritesModel,
    private val sessionManager: SessionManager
) : ViewModel() {

    lateinit var state: Observable<DeviationViewerViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(deviationId: String) {
        if (::state.isInitialized) return

        val initialState = DeviationViewerViewState(
                deviationId = deviationId,
                contentState = ContentState.Loading,
                isSignedIn = sessionManager.session is Session.User)

        state = loop.loop(initialState, LoadDeviationEffect(deviationId)).disposeOnDestroy()
    }

    fun dispatch(event: DeviationViewerEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<DeviationViewerEffect>): Observable<DeviationViewerEvent> {
        val loadEvents = effects.ofType<LoadDeviationEffect>()
                .switchMap { effect ->
                    deviationViewerModel.getDeviationById(effect.deviationId)
                            .map<DeviationViewerEvent> { result -> DeviationLoadedEvent(result) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }

        val downloadOriginalEvents = effects.ofType<DownloadOriginalEffect>()
                .switchMapMaybe { effect ->
                    downloadInfoRepository.getDownloadInfo(effect.deviationId)
                            .doOnSuccess { downloadInfo -> downloader.downloadPicture(Uri.parse(downloadInfo.downloadUrl)) }
                            .ignoreElement()
                            .toMaybe<DeviationViewerEvent>()
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> DownloadOriginalErrorEvent(error) }
                }

        val setFavoriteEvents = effects.ofType<SetFavoriteEffect>()
                .switchMapSingle { effect ->
                    favoritesModel.setFavorite(effect.deviationId, effect.favorite)
                            .toSingleDefault<DeviationViewerEvent>(SetFavoriteFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> SetFavoriteErrorEvent(error) }
                }

        return Observable.merge(loadEvents, downloadOriginalEvents, setFavoriteEvents)
    }

    private fun externalEvents(): Observable<DeviationViewerEvent> {
        return sessionManager.sessionChanges
                .bindToLifecycle()
                .map { session -> SessionChangedEvent(session) }
    }

    companion object {
        private const val LOG_TAG = "DeviationViewerViewModel"
    }
}

class DeviationViewerStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : Reducer<DeviationViewerEvent, DeviationViewerViewState, DeviationViewerEffect> {

    override fun invoke(state: DeviationViewerViewState, event: DeviationViewerEvent): Next<DeviationViewerViewState, DeviationViewerEffect> = when (event) {
        is DeviationLoadedEvent -> {
            next(state.copy(
                    contentState = ContentState.Content,
                    content = event.result.deviationContent,
                    infoViewState = event.result.infoViewState,
                    menuState = event.result.menuState))
        }

        is LoadErrorEvent -> {
            next(state.copy(contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error))))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadDeviationEffect(state.deviationId))
        }

        DownloadOriginalClickEvent -> {
            next(state, DownloadOriginalEffect(state.deviationId))
        }

        is DownloadOriginalErrorEvent -> {
            val errorMessage = errorMessageProvider.getErrorMessage(event.error, R.string.deviations_viewer_message_download_error)
            next(state.copy(snackbarState = SnackbarState.Message(errorMessage)))
        }

        is SetFavoriteEvent -> {
            next(state.copy(showProgressDialog = true), SetFavoriteEffect(event.deviationId, event.favorite))
        }

        SetFavoriteFinishedEvent -> {
            next(state.copy(showProgressDialog = false))
        }

        is SetFavoriteErrorEvent -> {
            val errorMessage = errorMessageProvider.getErrorMessage(event.error)
            next(state.copy(showProgressDialog = false, snackbarState = SnackbarState.Message(errorMessage)))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is SessionChangedEvent -> {
            next(state.copy(isSignedIn = event.session is Session.User))
        }
    }
}
