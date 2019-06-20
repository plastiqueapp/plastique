package io.plastique.watch

import androidx.core.text.htmlEncode
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
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.network.NetworkConnectionState
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NetworkConnectivityMonitor
import io.plastique.core.network.NoNetworkConnectionException
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.watch.WatcherListEffect.LoadMoreEffect
import io.plastique.watch.WatcherListEffect.LoadWatchersEffect
import io.plastique.watch.WatcherListEffect.RefreshEffect
import io.plastique.watch.WatcherListEvent.ConnectionStateChangedEvent
import io.plastique.watch.WatcherListEvent.ItemsChangedEvent
import io.plastique.watch.WatcherListEvent.LoadErrorEvent
import io.plastique.watch.WatcherListEvent.LoadMoreErrorEvent
import io.plastique.watch.WatcherListEvent.LoadMoreEvent
import io.plastique.watch.WatcherListEvent.LoadMoreFinishedEvent
import io.plastique.watch.WatcherListEvent.RefreshErrorEvent
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RefreshFinishedEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SessionChangedEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class WatcherListViewModel @Inject constructor(
    stateReducer: WatcherListStateReducer,
    effectHandlerFactory: WatcherListEffectHandlerFactory,
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    lateinit var state: Observable<WatcherListViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String?) {
        if (::state.isInitialized) return

        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(WatcherListViewState(
                username = username,
                contentState = ContentState.Empty(EmptyState.MessageWithButton(
                    messageResId = R.string.watch_message_sign_in,
                    buttonTextId = R.string.common_button_sign_in)),
                signInNeeded = signInNeeded))
        } else {
            next(WatcherListViewState(
                username = username,
                contentState = ContentState.Loading,
                signInNeeded = signInNeeded),
                LoadWatchersEffect(username))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: WatcherListEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<WatcherListEvent> {
        return Observable.merge(
            connectivityMonitor.connectionState
                .valveLatest(screenVisible)
                .map { connectionState -> ConnectionStateChangedEvent(connectionState) },
            sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) })
    }

    private companion object {
        private const val LOG_TAG = "WatcherListViewModel"
    }
}

@AutoFactory
class WatcherListEffectHandler(
    @Provided private val dataSource: WatcherDataSource,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<WatcherListEffect, WatcherListEvent> {

    override fun handle(effects: Observable<WatcherListEffect>): Observable<WatcherListEvent> {
        val loadWatchersEvents = effects.ofType<LoadWatchersEffect>()
            .switchMap { effect ->
                dataSource.getData(effect.username)
                    .valveLatest(screenVisible)
                    .map<WatcherListEvent> { pagedData ->
                        val items = pagedData.value.map { WatcherItem(it) }
                        ItemsChangedEvent(items, pagedData.hasMore)
                    }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .switchMap {
                dataSource.loadMore()
                    .toSingleDefault<WatcherListEvent>(LoadMoreFinishedEvent)
                    .doOnError(Timber::e)
                    .toObservable()
                    .onErrorReturn { error -> LoadMoreErrorEvent(error) }
            }

        val refreshEvents = effects.ofType<RefreshEffect>()
            .switchMap {
                dataSource.refresh()
                    .toSingleDefault<WatcherListEvent>(RefreshFinishedEvent)
                    .doOnError(Timber::e)
                    .toObservable()
                    .onErrorReturn { error -> RefreshErrorEvent(error) }
            }

        return Observable.merge(loadWatchersEvents, loadMoreEvents, refreshEvents)
    }
}

class WatcherListStateReducer @Inject constructor(
    private val connectivityChecker: NetworkConnectivityChecker,
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<WatcherListEvent, WatcherListViewState, WatcherListEffect> {

    override fun reduce(state: WatcherListViewState, event: WatcherListEvent): StateWithEffects<WatcherListViewState, WatcherListEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyState = if (state.username != null) {
                    EmptyState.Message(R.string.watch_message_empty, listOf(state.username.htmlEncode()))
                } else {
                    EmptyState.Message(R.string.watch_message_empty_current_user)
                }
                ContentState.Empty(emptyState)
            }
            next(state.copy(
                contentState = contentState,
                items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                watcherItems = event.items,
                hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                contentState = ContentState.Empty(isError = true, error = event.error, emptyState = errorMessageProvider.getErrorState(event.error)),
                items = emptyList(),
                watcherItems = emptyList(),
                hasMore = false))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadWatchersEffect(state.username))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityChecker.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.watcherItems + LoadingIndicatorItem), LoadMoreEffect)
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
                items = state.watcherItems,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
        }

        RefreshEvent -> {
            next(state.copy(isRefreshing = true), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(isRefreshing = false))
        }

        is RefreshErrorEvent -> {
            next(state.copy(isRefreshing = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is ConnectionStateChangedEvent -> {
            if (event.connectionState == NetworkConnectionState.Connected &&
                state.contentState is ContentState.Empty &&
                state.contentState.error is NoNetworkConnectionException) {
                next(state.copy(contentState = ContentState.Loading), LoadWatchersEffect(state.username))
            } else {
                next(state)
            }
        }

        is SessionChangedEvent -> {
            val signInNeeded = state.username == null && event.session !is Session.User
            if (signInNeeded != state.signInNeeded) {
                if (signInNeeded) {
                    next(state.copy(
                        contentState = ContentState.Empty(EmptyState.MessageWithButton(
                            messageResId = R.string.watch_message_sign_in,
                            buttonTextId = R.string.common_button_sign_in)),
                        signInNeeded = signInNeeded,
                        items = emptyList(),
                        watcherItems = emptyList(),
                        hasMore = false))
                } else {
                    next(state.copy(
                        contentState = ContentState.Loading,
                        signInNeeded = signInNeeded
                    ), LoadWatchersEffect(state.username))
                }
            } else {
                next(state)
            }
        }
    }
}
