package io.plastique.gallery

import androidx.core.text.HtmlCompat
import androidx.core.text.htmlEncode
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import com.sch.rxjava2.extensions.valveLatest
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.BaseViewModel
import io.plastique.core.ResourceProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.gallery.GalleryEffect.LoadGalleryEffect
import io.plastique.gallery.GalleryEffect.LoadMoreEffect
import io.plastique.gallery.GalleryEffect.RefreshEffect
import io.plastique.gallery.GalleryEvent.CreateFolderEvent
import io.plastique.gallery.GalleryEvent.DeleteFolderEvent
import io.plastique.gallery.GalleryEvent.ItemsChangedEvent
import io.plastique.gallery.GalleryEvent.LoadErrorEvent
import io.plastique.gallery.GalleryEvent.LoadMoreErrorEvent
import io.plastique.gallery.GalleryEvent.LoadMoreEvent
import io.plastique.gallery.GalleryEvent.LoadMoreFinishedEvent
import io.plastique.gallery.GalleryEvent.RefreshErrorEvent
import io.plastique.gallery.GalleryEvent.RefreshEvent
import io.plastique.gallery.GalleryEvent.RefreshFinishedEvent
import io.plastique.gallery.GalleryEvent.RetryClickEvent
import io.plastique.gallery.GalleryEvent.SessionChangedEvent
import io.plastique.gallery.GalleryEvent.ShowMatureChangedEvent
import io.plastique.gallery.GalleryEvent.SnackbarShownEvent
import io.plastique.inject.scopes.FragmentScope
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class GalleryViewModel @Inject constructor(
    stateReducer: GalleryStateReducer,
    effectHandlerFactory: GalleryEffectHandlerFactory,
    private val resourceProvider: ResourceProvider,
    private val sessionManager: SessionManager,
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    lateinit var state: Observable<GalleryViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String?) {
        if (::state.isInitialized) return

        val params = FolderLoadParams(username = username, matureContent = contentSettings.showMature)
        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(GalleryViewState(
                params = params,
                contentState = ContentState.Empty(EmptyState.MessageWithButton(
                    message = resourceProvider.getString(R.string.gallery_message_sign_in),
                    button = resourceProvider.getString(R.string.common_button_sign_in))),
                signInNeeded = signInNeeded))
        } else {
            next(GalleryViewState(
                params = params,
                contentState = ContentState.Loading,
                signInNeeded = signInNeeded),
                LoadGalleryEffect(params))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: GalleryEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<GalleryEvent> {
        return Observable.merge(
            sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) },
            contentSettings.showMatureChanges
                .valveLatest(screenVisible)
                .map { showMature -> ShowMatureChangedEvent(showMature) })
    }

    companion object {
        private const val LOG_TAG = "GalleryViewModel"
    }
}

@AutoFactory
class GalleryEffectHandler(
    @Provided private val dataSource: FoldersWithDeviationsDataSource,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<GalleryEffect, GalleryEvent> {

    override fun handle(effects: Observable<GalleryEffect>): Observable<GalleryEvent> {
        val loadEvents = effects.ofType<LoadGalleryEffect>()
            .switchMap { effect ->
                dataSource.items(effect.params)
                    .valveLatest(screenVisible)
                    .map<GalleryEvent> { pagedData -> ItemsChangedEvent(items = pagedData.items, hasMore = pagedData.hasMore) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .switchMapSingle {
                dataSource.loadMore()
                    .toSingleDefault<GalleryEvent>(LoadMoreFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadMoreErrorEvent(error) }
            }

        val refreshEvents = effects.ofType<RefreshEffect>()
            .switchMapSingle {
                dataSource.refresh()
                    .toSingleDefault<GalleryEvent>(RefreshFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> RefreshErrorEvent(error) }
            }

        return Observable.merge(loadEvents, loadMoreEvents, refreshEvents)
    }
}

class GalleryStateReducer @Inject constructor(
    private val connectivityChecker: NetworkConnectivityChecker,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : StateReducer<GalleryEvent, GalleryViewState, GalleryEffect> {

    override fun reduce(state: GalleryViewState, event: GalleryEvent): StateWithEffects<GalleryViewState, GalleryEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyMessage = if (state.params.username != null) {
                    HtmlCompat.fromHtml(resourceProvider.getString(R.string.gallery_message_empty_user_collection, state.params.username.htmlEncode()), 0)
                } else {
                    resourceProvider.getString(R.string.gallery_message_empty_collection)
                }

                ContentState.Empty(EmptyState.Message(emptyMessage))
            }
            next(state.copy(
                contentState = contentState,
                items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                galleryItems = event.items,
                hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error)),
                items = emptyList(),
                galleryItems = emptyList(),
                hasMore = false))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityChecker.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.galleryItems + LoadingIndicatorItem), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(isLoadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(
                isLoadingMore = false,
                items = state.galleryItems,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error))))
        }

        RefreshEvent -> {
            next(state.copy(isRefreshing = true), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(isRefreshing = false))
        }

        is RefreshErrorEvent -> {
            next(state.copy(isRefreshing = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error))))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadGalleryEffect(state.params))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is SessionChangedEvent -> {
            val signInNeeded = state.params.username == null && event.session !is Session.User
            if (signInNeeded != state.signInNeeded) {
                if (signInNeeded) {
                    next(state.copy(
                        contentState = ContentState.Empty(EmptyState.MessageWithButton(
                            message = resourceProvider.getString(R.string.gallery_message_sign_in),
                            button = resourceProvider.getString(R.string.common_button_sign_in))),
                        signInNeeded = signInNeeded))
                } else {
                    next(state.copy(
                        contentState = ContentState.Loading,
                        signInNeeded = signInNeeded
                    ), LoadGalleryEffect(state.params))
                }
            } else {
                next(state)
            }
        }

        is ShowMatureChangedEvent -> {
            if (state.params.matureContent != event.showMature) {
                val params = state.params.copy(matureContent = event.showMature)
                next(state.copy(
                    params = params,
                    contentState = ContentState.Loading,
                    items = emptyList(),
                    galleryItems = emptyList()),
                    LoadGalleryEffect(params))
            } else {
                next(state)
            }
        }

        is CreateFolderEvent -> {
            // TODO
            next(state)
        }

        is DeleteFolderEvent -> {
            // TODO
            next(state)
        }
    }
}
