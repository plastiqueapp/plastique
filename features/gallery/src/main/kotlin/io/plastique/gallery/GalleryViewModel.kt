package io.plastique.gallery

import android.text.TextUtils
import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.exceptions.ApiResponseException
import io.plastique.core.flow.Effect
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.deviations.ContentSettings
import io.plastique.gallery.GalleryEffect.LoadEffect
import io.plastique.gallery.GalleryEffect.LoadMoreEffect
import io.plastique.gallery.GalleryEffect.RefreshEffect
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
import io.plastique.util.HtmlCompat
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class GalleryViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val dataSource: FoldersWithDeviationsDataSource,
    private val resourceProvider: ResourceProvider,
    private val sessionManager: SessionManager,
    private val contentSettings: ContentSettings
) : ViewModel() {

    lateinit var state: Observable<GalleryViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG)
    )

    fun init(username: String?) {
        if (::state.isInitialized) return

        val params = FolderLoadParams(username = username, matureContent = contentSettings.showMature)
        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(GalleryViewState(
                    params = params,
                    contentState = ContentState.Empty(EmptyState(
                            message = resourceProvider.getString(R.string.gallery_message_login),
                            button = resourceProvider.getString(R.string.gallery_button_login))),
                    signInNeeded = signInNeeded))
        } else {
            next(GalleryViewState(
                    params = params,
                    contentState = ContentState.Loading,
                    signInNeeded = signInNeeded),
                    LoadEffect(params))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: GalleryEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<GalleryEffect>): Observable<GalleryEvent> {
        val loadEvents = effects.ofType<LoadEffect>()
                .switchMap { effect ->
                    dataSource.items(effect.params)
                            .bindToLifecycle()
                            .map<GalleryEvent> { pagedData -> ItemsChangedEvent(items = pagedData.items, hasMore = pagedData.hasMore) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error, username = effect.params.username) }
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

    private fun externalEvents(): Observable<GalleryEvent> {
        return Observable.merge(
                sessionManager.sessionChanges
                        .bindToLifecycle()
                        .map { session -> SessionChangedEvent(session) },
                contentSettings.showMatureChanges
                        .bindToLifecycle()
                        .map { showMature -> ShowMatureChangedEvent(showMature) })
    }

    companion object {
        private const val LOG_TAG = "GalleryViewModel"
    }
}

class StateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : Reducer<GalleryEvent, GalleryViewState, GalleryEffect> {
    override fun invoke(state: GalleryViewState, event: GalleryEvent): Next<GalleryViewState, GalleryEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyMessage = if (state.params.username != null) {
                    HtmlCompat.fromHtml(resourceProvider.getString(R.string.gallery_message_empty_user_collection, TextUtils.htmlEncode(state.params.username)))
                } else {
                    resourceProvider.getString(R.string.gallery_message_empty_collection)
                }

                ContentState.Empty(EmptyState(message = emptyMessage))
            }
            next(state.copy(
                    contentState = contentState,
                    items = if (state.loadingMore) event.items + LoadingIndicatorItem else event.items,
                    galleryItems = event.items,
                    hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            val errorState = when (event.error) {
                is ApiResponseException -> EmptyState(
                        message = HtmlCompat.fromHtml(resourceProvider.getString(R.string.common_message_user_not_found, TextUtils.htmlEncode(event.username))))
                else -> errorMessageProvider.getErrorState(event.error)
            }
            next(state.copy(
                    contentState = ContentState.Empty(isError = true, emptyState = errorState),
                    items = emptyList(),
                    galleryItems = emptyList(),
                    hasMore = false))
        }

        LoadMoreEvent -> {
            if (!state.loadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(loadingMore = true, items = state.galleryItems + LoadingIndicatorItem), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(loadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(loadingMore = false, items = state.galleryItems, snackbarMessage = errorMessageProvider.getErrorMessage(event.error)))
        }

        RefreshEvent -> {
            next(state.copy(refreshing = true), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(refreshing = false))
        }

        is RefreshErrorEvent -> {
            next(state.copy(refreshing = false, snackbarMessage = errorMessageProvider.getErrorMessage(event.error)))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadEffect(state.params))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarMessage = null))
        }

        is SessionChangedEvent -> {
            val signInNeeded = state.params.username == null && event.session !is Session.User
            if (signInNeeded != state.signInNeeded) {
                if (signInNeeded) {
                    next(state.copy(
                            contentState = ContentState.Empty(EmptyState(
                                    message = resourceProvider.getString(R.string.gallery_message_login),
                                    button = resourceProvider.getString(R.string.gallery_button_login))),
                            signInNeeded = signInNeeded))
                } else {
                    next(state.copy(
                            contentState = ContentState.Loading,
                            signInNeeded = signInNeeded
                    ), LoadEffect(state.params))
                }
            } else {
                next(state)
            }
        }

        is ShowMatureChangedEvent -> {
            if (state.params.matureContent != event.showMature) {
                val params = state.params.copy(matureContent = event.showMature)
                next(state.copy(
                        contentState = ContentState.Loading,
                        items = emptyList(),
                        params = params),
                        LoadEffect(params))
            } else {
                next(state)
            }
        }
    }
}

sealed class GalleryEvent : Effect() {
    data class ItemsChangedEvent(val items: List<ListItem>, val hasMore: Boolean) : GalleryEvent() {
        override fun toString(): String =
                "ItemsChangedEvent(items=${items.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable, val username: String?) : GalleryEvent()

    object LoadMoreEvent : GalleryEvent()
    object LoadMoreFinishedEvent : GalleryEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : GalleryEvent()

    object RefreshEvent : GalleryEvent()
    object RefreshFinishedEvent : GalleryEvent()
    data class RefreshErrorEvent(val error: Throwable) : GalleryEvent()

    object RetryClickEvent : GalleryEvent()
    object SnackbarShownEvent : GalleryEvent()

    data class SessionChangedEvent(val session: Session) : GalleryEvent()
    data class ShowMatureChangedEvent(val showMature: Boolean) : GalleryEvent()
}

sealed class GalleryEffect : Effect() {
    data class LoadEffect(val params: FolderLoadParams) : GalleryEffect()
    object LoadMoreEffect : GalleryEffect()
    object RefreshEffect : GalleryEffect()
}
