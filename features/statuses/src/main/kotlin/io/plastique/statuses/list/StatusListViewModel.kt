package io.plastique.statuses.list

import android.text.TextUtils
import androidx.core.text.HtmlCompat
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
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.statuses.R
import io.plastique.statuses.StatusListLoadParams
import io.plastique.statuses.list.StatusListEffect.LoadMoreEffect
import io.plastique.statuses.list.StatusListEffect.LoadStatusesEffect
import io.plastique.statuses.list.StatusListEffect.RefreshEffect
import io.plastique.statuses.list.StatusListEvent.ItemsChangedEvent
import io.plastique.statuses.list.StatusListEvent.LoadErrorEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreErrorEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreFinishedEvent
import io.plastique.statuses.list.StatusListEvent.RefreshErrorEvent
import io.plastique.statuses.list.StatusListEvent.RefreshEvent
import io.plastique.statuses.list.StatusListEvent.RefreshFinishedEvent
import io.plastique.statuses.list.StatusListEvent.RetryClickEvent
import io.plastique.statuses.list.StatusListEvent.SessionChangedEvent
import io.plastique.statuses.list.StatusListEvent.ShowMatureChangedEvent
import io.plastique.statuses.list.StatusListEvent.SnackbarShownEvent
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class StatusListViewModel @Inject constructor(
    stateReducer: StatusListStateReducer,
    private val statusListModel: StatusListModel,
    private val sessionManager: SessionManager,
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    lateinit var state: Observable<StatusListViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(username: String) {
        if (::state.isInitialized) return

        val params = StatusListLoadParams(username = username, matureContent = contentSettings.showMature)
        val initialState = StatusListViewState(
                params = params,
                contentState = ContentState.Loading)

        state = loop.loop(initialState, LoadStatusesEffect(params)).disposeOnDestroy()
    }

    fun dispatch(event: StatusListEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<StatusListEffect>): Observable<StatusListEvent> {
        val itemEvents = effects.ofType<LoadStatusesEffect>()
                .switchMap { effect ->
                    statusListModel.getItems(effect.params)
                            .bindToLifecycle()
                            .map<StatusListEvent> { pagedData -> ItemsChangedEvent(items = pagedData.items, hasMore = pagedData.hasMore) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }

        val loadMoreEvents = effects.ofType<StatusListEffect.LoadMoreEffect>()
                .switchMapSingle {
                    statusListModel.loadMore()
                            .toSingleDefault<StatusListEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(error) }
                }

        val refreshEvents = effects.ofType<StatusListEffect.RefreshEffect>()
                .switchMapSingle {
                    statusListModel.refresh()
                            .toSingleDefault<StatusListEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(error) }
                }

        return Observable.merge(itemEvents, loadMoreEvents, refreshEvents)
    }

    private fun externalEvents(): Observable<StatusListEvent> {
        return Observable.merge(
                sessionManager.sessionChanges
                        .bindToLifecycle()
                        .map { session -> SessionChangedEvent(session) },
                contentSettings.showMatureChanges
                        .bindToLifecycle()
                        .map { showMature -> ShowMatureChangedEvent(showMature) })
    }

    companion object {
        private const val LOG_TAG = "StatusListViewModel"
    }
}

class StatusListStateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : Reducer<StatusListEvent, StatusListViewState, StatusListEffect> {
    override fun invoke(state: StatusListViewState, event: StatusListEvent): Next<StatusListViewState, StatusListEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyMessage = HtmlCompat.fromHtml(resourceProvider.getString(R.string.statuses_message_empty, TextUtils.htmlEncode(state.params.username)), 0)
                ContentState.Empty(EmptyState(message = emptyMessage))
            }
            next(state.copy(
                    contentState = contentState,
                    items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                    statusItems = event.items,
                    hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                    contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error)),
                    items = emptyList(),
                    statusItems = emptyList(),
                    hasMore = false))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.statusItems + LoadingIndicatorItem), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(isLoadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(isLoadingMore = false, items = state.statusItems, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error))))
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
            next(state.copy(contentState = ContentState.Loading), LoadStatusesEffect(state.params))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is SessionChangedEvent -> {
            // TODO
            next(state)
        }

        is ShowMatureChangedEvent -> {
            if (state.params.matureContent != event.showMature) {
                val params = state.params.copy(matureContent = event.showMature)
                next(state.copy(
                        params = params,
                        contentState = ContentState.Loading,
                        items = emptyList(),
                        statusItems = emptyList()),
                        LoadStatusesEffect(params))
            } else {
                next(state)
            }
        }
    }
}
