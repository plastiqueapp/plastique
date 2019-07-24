package io.plastique.deviations.viewer

import android.net.Uri
import com.github.technoir42.rxjava2.extensions.valveLatest
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.collections.FavoritesModel
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.content.ContentState
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
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
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteErrorEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteFinishedEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.UserChangedEvent
import io.plastique.util.FileDownloader
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class DeviationViewerViewModel @Inject constructor(
    stateReducer: DeviationViewerStateReducer,
    effectHandler: DeviationViewerEffectHandler,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    lateinit var state: Observable<DeviationViewerViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandler,
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

    private fun externalEvents(): Observable<DeviationViewerEvent> {
        return sessionManager.userIdChanges
            .skip(1)
            .valveLatest(screenVisible)
            .map { userId -> UserChangedEvent(userId.toNullable()) }
    }

    companion object {
        private const val LOG_TAG = "DeviationViewerViewModel"
    }
}

class DeviationViewerEffectHandler @Inject constructor(
    private val deviationViewerModel: DeviationViewerModel,
    private val downloadInfoRepository: DownloadInfoRepository,
    private val downloader: FileDownloader,
    private val favoritesModel: FavoritesModel
) : EffectHandler<DeviationViewerEffect, DeviationViewerEvent> {

    override fun handle(effects: Observable<DeviationViewerEffect>): Observable<DeviationViewerEvent> {
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
}

class DeviationViewerStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<DeviationViewerEvent, DeviationViewerViewState, DeviationViewerEffect> {

    override fun reduce(state: DeviationViewerViewState, event: DeviationViewerEvent): StateWithEffects<DeviationViewerViewState, DeviationViewerEffect> =
        when (event) {
            is DeviationLoadedEvent -> {
                next(state.copy(
                    contentState = ContentState.Content,
                    content = event.result.deviationContent,
                    infoViewState = event.result.infoViewState,
                    menuState = event.result.menuState,
                    emptyState = null))
            }

            is LoadErrorEvent -> {
                next(state.copy(contentState = ContentState.Empty, emptyState = errorMessageProvider.getErrorState(event.error)))
            }

            RetryClickEvent -> {
                next(state.copy(contentState = ContentState.Loading, emptyState = null), LoadDeviationEffect(state.deviationId))
            }

            DownloadOriginalClickEvent -> {
                next(state, DownloadOriginalEffect(state.deviationId))
            }

            is DownloadOriginalErrorEvent -> {
                next(state.copy(snackbarState = SnackbarState.Message(
                    errorMessageProvider.getErrorMessageId(event.error, R.string.deviations_viewer_message_download_error))))
            }

            is SetFavoriteEvent -> {
                next(state.copy(showProgressDialog = true), SetFavoriteEffect(state.deviationId, event.favorite))
            }

            SetFavoriteFinishedEvent -> {
                next(state.copy(showProgressDialog = false))
            }

            is SetFavoriteErrorEvent -> {
                next(state.copy(
                    showProgressDialog = false,
                    snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
            }

            SnackbarShownEvent -> {
                next(state.copy(snackbarState = null))
            }

            is UserChangedEvent -> {
                next(state.copy(isSignedIn = event.userId != null))
            }
        }
}
