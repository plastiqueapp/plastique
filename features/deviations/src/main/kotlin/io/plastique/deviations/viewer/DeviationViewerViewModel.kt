package io.plastique.deviations.viewer

import android.net.Uri
import com.sch.rxjava2.extensions.ofType
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
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.R
import io.plastique.deviations.download.DownloadInfoRepository
import io.plastique.deviations.viewer.DeviationViewerEffect.DownloadOriginalEffect
import io.plastique.deviations.viewer.DeviationViewerEffect.LoadDeviationEffect
import io.plastique.deviations.viewer.DeviationViewerEvent.DeviationLoadedEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalErrorEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.LoadErrorEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SessionChangedEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.deviations.viewer.DeviationViewerViewState.MenuState
import io.plastique.inject.scopes.ActivityScope
import io.plastique.util.FileDownloader
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class DeviationViewerViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val deviationRepository: DeviationRepository,
    private val downloadInfoRepository: DownloadInfoRepository,
    private val downloader: FileDownloader,
    private val errorMessageProvider: ErrorMessageProvider,
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
                signedIn = sessionManager.session is Session.User)

        state = loop.loop(initialState, LoadDeviationEffect(deviationId)).disposeOnDestroy()
    }

    fun dispatch(event: DeviationViewerEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<DeviationViewerEffect>): Observable<DeviationViewerEvent> {
        val loadEvents = effects.ofType<LoadDeviationEffect>()
                .switchMap { effect ->
                    deviationRepository.getDeviationById(effect.deviationId)
                            .map<DeviationViewerEvent> { deviation -> DeviationLoadedEvent(deviation) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(errorMessageProvider.getErrorState(error)) }
                }

        val downloadOriginalEvents = effects.ofType<DownloadOriginalEffect>()
                .switchMapMaybe { effect ->
                    downloadInfoRepository.getDownloadInfo(effect.deviationId)
                            .doOnSuccess { downloadInfo -> downloader.downloadPicture(Uri.parse(downloadInfo.downloadUrl)) }
                            .ignoreElement()
                            .toMaybe<DeviationViewerEvent>()
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> DownloadOriginalErrorEvent(errorMessageProvider.getErrorMessage(error, R.string.deviations_viewer_message_download_error)) }
                }

        return Observable.merge(loadEvents, downloadOriginalEvents)
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

class StateReducer @Inject constructor() : Reducer<DeviationViewerEvent, DeviationViewerViewState, DeviationViewerEffect> {
    override fun invoke(state: DeviationViewerViewState, event: DeviationViewerEvent): Next<DeviationViewerViewState, DeviationViewerEffect> = when (event) {
        is DeviationLoadedEvent -> {
            val menuState = MenuState(
                    showDownload = event.deviation.properties.isDownloadable,
                    showFavorite = state.signedIn,
                    isFavoriteChecked = event.deviation.properties.isFavorite)

            next(state.copy(
                    contentState = ContentState.Content,
                    deviation = event.deviation,
                    menuState = menuState))
        }

        is LoadErrorEvent -> {
            next(state.copy(contentState = ContentState.Empty(event.emptyState, isError = true)))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadDeviationEffect(state.deviationId))
        }

        DownloadOriginalClickEvent -> {
            next(state, DownloadOriginalEffect(state.deviationId))
        }

        is DownloadOriginalErrorEvent -> {
            next(state.copy(snackbarMessage = event.errorMessage))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarMessage = null))
        }

        is SessionChangedEvent -> {
            val signedIn = event.session is Session.User
            next(state.copy(
                    signedIn = signedIn,
                    menuState = state.menuState.copy(showFavorite = signedIn)))
        }
    }
}
