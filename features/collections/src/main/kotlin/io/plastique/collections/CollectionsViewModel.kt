package io.plastique.collections

import android.text.TextUtils
import com.sch.rxjava2.extensions.ofType
import io.plastique.collections.CollectionsEffect.LoadCollectionsEffect
import io.plastique.collections.CollectionsEffect.LoadMoreEffect
import io.plastique.collections.CollectionsEffect.RefreshEffect
import io.plastique.collections.CollectionsEvent.CreateFolderEvent
import io.plastique.collections.CollectionsEvent.ItemsChangedEvent
import io.plastique.collections.CollectionsEvent.LoadErrorEvent
import io.plastique.collections.CollectionsEvent.LoadMoreErrorEvent
import io.plastique.collections.CollectionsEvent.LoadMoreEvent
import io.plastique.collections.CollectionsEvent.LoadMoreFinishedEvent
import io.plastique.collections.CollectionsEvent.RefreshErrorEvent
import io.plastique.collections.CollectionsEvent.RefreshEvent
import io.plastique.collections.CollectionsEvent.RefreshFinishedEvent
import io.plastique.collections.CollectionsEvent.RetryClickEvent
import io.plastique.collections.CollectionsEvent.SessionChangedEvent
import io.plastique.collections.CollectionsEvent.ShowMatureChangedEvent
import io.plastique.collections.CollectionsEvent.SnackbarShownEvent
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.exceptions.ApiResponseException
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.deviations.ContentSettings
import io.plastique.inject.scopes.FragmentScope
import io.plastique.util.HtmlCompat
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class CollectionsViewModel @Inject constructor(
    stateReducer: CollectionsStateReducer,
    private val sessionManager: SessionManager,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider,
    private val dataSource: FoldersWithDeviationsDataSource,
    private val contentSettings: ContentSettings
) : ViewModel() {

    lateinit var state: Observable<CollectionsViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(username: String?) {
        if (::state.isInitialized) return

        val params = FolderLoadParams(username = username, matureContent = contentSettings.showMature)
        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(CollectionsViewState(
                    params = params,
                    contentState = ContentState.Empty(EmptyState(
                            message = resourceProvider.getString(R.string.collections_message_login),
                            button = resourceProvider.getString(R.string.common_button_login))),
                    signInNeeded = signInNeeded))
        } else {
            next(CollectionsViewState(
                    params = params,
                    contentState = ContentState.Loading,
                    signInNeeded = signInNeeded),
                    LoadCollectionsEffect(params))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: CollectionsEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<CollectionsEffect>): Observable<CollectionsEvent> {
        val loadCollectionsEvent = effects.ofType<LoadCollectionsEffect>()
                .switchMap { effect ->
                    dataSource.items(effect.params)
                            .bindToLifecycle()
                            .map<CollectionsEvent> { pagedData -> ItemsChangedEvent(items = pagedData.items, hasMore = pagedData.hasMore) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(getErrorState(error, username = effect.params.username)) }
                }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .switchMapSingle {
                    dataSource.loadMore()
                            .toSingleDefault<CollectionsEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(errorMessageProvider.getErrorMessage(error)) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .switchMapSingle {
                    dataSource.refresh()
                            .toSingleDefault<CollectionsEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(errorMessageProvider.getErrorMessage(error)) }
                }

        return Observable.merge(loadCollectionsEvent, loadMoreEvents, refreshEvents)
    }

    private fun externalEvents(): Observable<CollectionsEvent> {
        return Observable.merge(
                sessionManager.sessionChanges
                        .bindToLifecycle()
                        .map { session -> SessionChangedEvent(session) },
                contentSettings.showMatureChanges
                        .bindToLifecycle()
                        .map { showMature -> ShowMatureChangedEvent(showMature) })
    }

    private fun getErrorState(error: Throwable, username: String?): EmptyState = when (error) {
        is ApiResponseException -> EmptyState(
                message = HtmlCompat.fromHtml(resourceProvider.getString(R.string.common_message_user_not_found, TextUtils.htmlEncode(username))))
        else -> errorMessageProvider.getErrorState(error)
    }

    companion object {
        private const val LOG_TAG = "CollectionsViewModel"
    }
}

class CollectionsStateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val resourceProvider: ResourceProvider
) : Reducer<CollectionsEvent, CollectionsViewState, CollectionsEffect> {
    override fun invoke(state: CollectionsViewState, event: CollectionsEvent): Next<CollectionsViewState, CollectionsEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyMessage = if (state.params.username != null) {
                    HtmlCompat.fromHtml(resourceProvider.getString(R.string.collections_message_empty_user_collection, TextUtils.htmlEncode(state.params.username)))
                } else {
                    resourceProvider.getString(R.string.collections_message_empty_collection)
                }

                ContentState.Empty(EmptyState(message = emptyMessage))
            }
            next(state.copy(
                    contentState = contentState,
                    items = if (state.loadingMore) event.items + LoadingIndicatorItem else event.items,
                    collectionItems = event.items,
                    hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                    contentState = ContentState.Empty(isError = true, emptyState = event.errorState),
                    items = emptyList(),
                    collectionItems = emptyList(),
                    hasMore = false))
        }

        LoadMoreEvent -> {
            if (!state.loadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(loadingMore = true, items = state.collectionItems + LoadingIndicatorItem), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(loadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(loadingMore = false, items = state.collectionItems, snackbarMessage = event.errorMessage))
        }

        RefreshEvent -> {
            next(state.copy(refreshing = true), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(refreshing = false))
        }

        is RefreshErrorEvent -> {
            next(state.copy(refreshing = false, snackbarMessage = event.errorMessage))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCollectionsEffect(state.params))
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
                                    message = resourceProvider.getString(R.string.collections_message_login),
                                    button = resourceProvider.getString(R.string.common_button_login))),
                            signInNeeded = signInNeeded))
                } else {
                    next(state.copy(
                            contentState = ContentState.Loading,
                            signInNeeded = signInNeeded
                    ), LoadCollectionsEffect(state.params))
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
                        LoadCollectionsEffect(params))
            } else {
                next(state)
            }
        }

        is CreateFolderEvent -> {
            // TODO
            next(state)
        }
    }
}
