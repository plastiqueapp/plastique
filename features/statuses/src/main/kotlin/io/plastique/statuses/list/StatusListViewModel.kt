package io.plastique.statuses.list

import androidx.core.text.htmlEncode
import com.github.technoir42.rxjava2.extensions.surroundWith
import com.github.technoir42.rxjava2.extensions.valveLatest
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.lists.PagedListState
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.statuses.R
import io.plastique.statuses.StatusListLoadParams
import io.plastique.statuses.StatusesNavigator
import io.plastique.statuses.list.StatusListEffect.LoadMoreEffect
import io.plastique.statuses.list.StatusListEffect.LoadStatusesEffect
import io.plastique.statuses.list.StatusListEffect.RefreshEffect
import io.plastique.statuses.list.StatusListEvent.ItemsChangedEvent
import io.plastique.statuses.list.StatusListEvent.LoadErrorEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreErrorEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreFinishedEvent
import io.plastique.statuses.list.StatusListEvent.LoadMoreStartedEvent
import io.plastique.statuses.list.StatusListEvent.RefreshErrorEvent
import io.plastique.statuses.list.StatusListEvent.RefreshEvent
import io.plastique.statuses.list.StatusListEvent.RefreshFinishedEvent
import io.plastique.statuses.list.StatusListEvent.RetryClickEvent
import io.plastique.statuses.list.StatusListEvent.ShowMatureChangedEvent
import io.plastique.statuses.list.StatusListEvent.SnackbarShownEvent
import io.plastique.statuses.list.StatusListEvent.UserChangedEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class StatusListViewModel @Inject constructor(
    stateReducer: StatusListStateReducer,
    effectHandlerFactory: StatusListEffectHandlerFactory,
    val navigator: StatusesNavigator,
    private val sessionManager: SessionManager,
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    lateinit var state: Observable<StatusListViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
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

    private fun externalEvents(): Observable<StatusListEvent> {
        return Observable.merge(
            contentSettings.showMatureChanges
                .valveLatest(screenVisible)
                .map { showMature -> ShowMatureChangedEvent(showMature) },
            sessionManager.userIdChanges
                .skip(1)
                .valveLatest(screenVisible)
                .map { userId -> UserChangedEvent(userId.toNullable()) })
    }

    companion object {
        private const val LOG_TAG = "StatusListViewModel"
    }
}

@AutoFactory
class StatusListEffectHandler(
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
    @Provided private val statusListModel: StatusListModel,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<StatusListEffect, StatusListEvent> {

    override fun handle(effects: Observable<StatusListEffect>): Observable<StatusListEvent> {
        val itemEvents = effects.ofType<LoadStatusesEffect>()
            .switchMap { effect ->
                statusListModel.getItems(effect.params)
                    .valveLatest(screenVisible)
                    .map<StatusListEvent> { pagedData -> ItemsChangedEvent(items = pagedData.items, hasMore = pagedData.hasMore) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                statusListModel.loadMore()
                    .toObservable<StatusListEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadMoreErrorEvent(error) }
            }

        val refreshEvents = effects.ofType<RefreshEffect>()
            .switchMapSingle {
                statusListModel.refresh()
                    .toSingleDefault<StatusListEvent>(RefreshFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> RefreshErrorEvent(error) }
            }

        return Observable.merge(itemEvents, loadMoreEvents, refreshEvents)
    }
}

class StatusListStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<StatusListEvent, StatusListViewState, StatusListEffect> {

    override fun reduce(state: StatusListViewState, event: StatusListEvent): StateWithEffects<StatusListViewState, StatusListEffect> = when (event) {
        is ItemsChangedEvent -> {
            if (event.items.isNotEmpty()) {
                next(state.copy(
                    contentState = ContentState.Content,
                    listState = state.listState.copy(
                        items = if (state.listState.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                        contentItems = event.items,
                        hasMore = event.hasMore),
                    emptyState = null))
            } else {
                next(state.copy(
                    contentState = ContentState.Empty,
                    listState = PagedListState.Empty,
                    emptyState = EmptyState.Message(R.string.statuses_message_empty, listOf(state.params.username.htmlEncode()))))
            }
        }

        is LoadErrorEvent -> {
            next(state.copy(
                contentState = ContentState.Empty,
                listState = PagedListState.Empty,
                emptyState = errorMessageProvider.getErrorState(event.error)))
        }

        LoadMoreEvent -> {
            if (!state.listState.isLoadingMore) {
                next(state, LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreStartedEvent -> {
            next(state.copy(listState = state.listState.copy(isLoadingMore = true, items = state.listState.contentItems + LoadingIndicatorItem)))
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(listState = state.listState.copy(isLoadingMore = false)))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(
                listState = state.listState.copy(isLoadingMore = false, items = state.listState.contentItems),
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
        }

        RefreshEvent -> {
            next(state.copy(listState = state.listState.copy(isRefreshing = true)), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(listState = state.listState.copy(isRefreshing = false)))
        }

        is RefreshErrorEvent -> {
            next(state.copy(
                listState = state.listState.copy(isRefreshing = false),
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading, emptyState = null), LoadStatusesEffect(state.params))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = null))
        }

        is UserChangedEvent -> {
            // TODO
            next(state)
        }

        is ShowMatureChangedEvent -> {
            if (state.params.matureContent != event.showMature) {
                val params = state.params.copy(matureContent = event.showMature)
                next(state.copy(
                    params = params,
                    contentState = ContentState.Loading,
                    listState = PagedListState.Empty,
                    emptyState = null),
                    LoadStatusesEffect(params))
            } else {
                next(state)
            }
        }
    }
}
