package io.plastique.collections

import android.text.TextUtils
import androidx.core.text.HtmlCompat
import io.plastique.collections.CollectionsEffect.LoadCollectionsEffect
import io.plastique.collections.CollectionsEffect.LoadMoreEffect
import io.plastique.collections.CollectionsEffect.RefreshEffect
import io.plastique.collections.CollectionsEvent.CreateFolderEvent
import io.plastique.collections.CollectionsEvent.DeleteFolderEvent
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
import io.plastique.core.BaseViewModel
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.inject.scopes.FragmentScope
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class CollectionsViewModel @Inject constructor(
    stateReducer: CollectionsStateReducer,
    private val sessionManager: SessionManager,
    private val resourceProvider: ResourceProvider,
    private val dataSource: FoldersWithDeviationsDataSource,
    private val contentSettings: ContentSettings
) : BaseViewModel() {

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
                    contentState = ContentState.Empty(EmptyState.MessageWithButton(
                            message = resourceProvider.getString(R.string.collections_message_sign_in),
                            button = resourceProvider.getString(R.string.common_button_sign_in))),
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
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .switchMapSingle {
                    dataSource.loadMore()
                            .toSingleDefault<CollectionsEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(error) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .switchMapSingle {
                    dataSource.refresh()
                            .toSingleDefault<CollectionsEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(error) }
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

    companion object {
        private const val LOG_TAG = "CollectionsViewModel"
    }
}

class CollectionsStateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : Reducer<CollectionsEvent, CollectionsViewState, CollectionsEffect> {
    override fun invoke(state: CollectionsViewState, event: CollectionsEvent): Next<CollectionsViewState, CollectionsEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyMessage = if (state.params.username != null) {
                    HtmlCompat.fromHtml(resourceProvider.getString(R.string.collections_message_empty_user_collection, TextUtils.htmlEncode(state.params.username)), 0)
                } else {
                    resourceProvider.getString(R.string.collections_message_empty_collection)
                }

                ContentState.Empty(EmptyState.Message(emptyMessage))
            }
            next(state.copy(
                    contentState = contentState,
                    items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                    collectionItems = event.items,
                    hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                    contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error)),
                    items = emptyList(),
                    collectionItems = emptyList(),
                    hasMore = false))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.collectionItems + LoadingIndicatorItem), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(isLoadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(isLoadingMore = false, items = state.collectionItems, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error))))
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
            next(state.copy(contentState = ContentState.Loading), LoadCollectionsEffect(state.params))
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
                                    message = resourceProvider.getString(R.string.collections_message_sign_in),
                                    button = resourceProvider.getString(R.string.common_button_sign_in))),
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
                        params = params,
                        contentState = ContentState.Loading,
                        items = emptyList(),
                        collectionItems = emptyList()),
                        LoadCollectionsEffect(params))
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
