package io.plastique.watch

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
import io.plastique.common.ErrorType
import io.plastique.common.toErrorType
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.lists.PagedListState
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.network.NetworkConnectionState
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NetworkConnectivityMonitor
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
import io.plastique.core.snackbar.SnackbarState
import io.plastique.watch.WatcherListEffect.LoadMoreEffect
import io.plastique.watch.WatcherListEffect.LoadWatchersEffect
import io.plastique.watch.WatcherListEffect.OpenSignInEffect
import io.plastique.watch.WatcherListEffect.RefreshEffect
import io.plastique.watch.WatcherListEvent.ConnectionStateChangedEvent
import io.plastique.watch.WatcherListEvent.ItemsChangedEvent
import io.plastique.watch.WatcherListEvent.LoadErrorEvent
import io.plastique.watch.WatcherListEvent.LoadMoreErrorEvent
import io.plastique.watch.WatcherListEvent.LoadMoreEvent
import io.plastique.watch.WatcherListEvent.LoadMoreFinishedEvent
import io.plastique.watch.WatcherListEvent.LoadMoreStartedEvent
import io.plastique.watch.WatcherListEvent.RefreshErrorEvent
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RefreshFinishedEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.plastique.watch.WatcherListEvent.UserChangedEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class WatcherListViewModel @Inject constructor(
    stateReducer: WatcherListStateReducer,
    effectHandlerFactory: WatcherListEffectHandlerFactory,
    val navigator: WatchNavigator,
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    lateinit var state: Observable<WatcherListViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(navigator, screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String?) {
        if (::state.isInitialized) return

        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(WatcherListViewState(
                username = username,
                contentState = ContentState.Empty,
                signInNeeded = signInNeeded,
                emptyState = EmptyState.MessageWithButton(
                    messageResId = R.string.watch_message_sign_in,
                    buttonTextId = R.string.common_button_sign_in)))
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
            sessionManager.userIdChanges
                .skip(1)
                .valveLatest(screenVisible)
                .map { userId -> UserChangedEvent(userId.toNullable()) })
    }

    private companion object {
        private const val LOG_TAG = "WatcherListViewModel"
    }
}

@AutoFactory
class WatcherListEffectHandler(
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
    @Provided private val dataSource: WatcherDataSource,
    private val navigator: WatchNavigator,
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
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                dataSource.loadMore()
                    .toObservable<WatcherListEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
                    .doOnError(Timber::e)
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

        val navigationEvents = effects.ofType<OpenSignInEffect>()
            .doOnNext { navigator.openSignIn() }
            .ignoreElements()
            .toObservable<WatcherListEvent>()

        return Observable.merge(loadWatchersEvents, loadMoreEvents, refreshEvents, navigationEvents)
    }
}

class WatcherListStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<WatcherListEvent, WatcherListViewState, WatcherListEffect> {

    override fun reduce(state: WatcherListViewState, event: WatcherListEvent): StateWithEffects<WatcherListViewState, WatcherListEffect> = when (event) {
        is ItemsChangedEvent -> {
            if (event.items.isNotEmpty()) {
                next(state.copy(
                    contentState = ContentState.Content,
                    errorType = ErrorType.None,
                    listState = state.listState.copy(
                        items = if (state.listState.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                        contentItems = event.items,
                        hasMore = event.hasMore),
                    emptyState = null))
            } else {
                val emptyState = if (state.username != null) {
                    EmptyState.Message(R.string.watch_message_empty, listOf(state.username.htmlEncode()))
                } else {
                    EmptyState.Message(R.string.watch_message_empty_current_user)
                }

                next(state.copy(
                    contentState = ContentState.Empty,
                    errorType = ErrorType.None,
                    listState = PagedListState.Empty,
                    emptyState = emptyState))
            }
        }

        is LoadErrorEvent -> {
            next(state.copy(
                contentState = ContentState.Empty,
                errorType = event.error.toErrorType(),
                listState = PagedListState.Empty,
                emptyState = errorMessageProvider.getErrorState(event.error)))
        }

        RetryClickEvent -> {
            if (!state.signInNeeded) {
                next(state.copy(contentState = ContentState.Loading, errorType = ErrorType.None, emptyState = null), LoadWatchersEffect(state.username))
            } else {
                next(state, OpenSignInEffect)
            }
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

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = null))
        }

        is ConnectionStateChangedEvent -> {
            if (event.connectionState == NetworkConnectionState.Connected &&
                state.contentState == ContentState.Empty &&
                state.errorType == ErrorType.NoNetworkConnection) {
                next(state.copy(contentState = ContentState.Loading, errorType = ErrorType.None, emptyState = null), LoadWatchersEffect(state.username))
            } else {
                next(state)
            }
        }

        is UserChangedEvent -> {
            val signInNeeded = state.username == null && event.userId == null
            if (signInNeeded != state.signInNeeded) {
                if (signInNeeded) {
                    next(state.copy(
                        contentState = ContentState.Empty,
                        errorType = ErrorType.Other,
                        signInNeeded = signInNeeded,
                        listState = PagedListState.Empty,
                        emptyState = EmptyState.MessageWithButton(
                            messageResId = R.string.watch_message_sign_in,
                            buttonTextId = R.string.common_button_sign_in)))
                } else {
                    next(state.copy(
                        contentState = ContentState.Loading,
                        errorType = ErrorType.None,
                        signInNeeded = signInNeeded,
                        emptyState = null),
                        LoadWatchersEffect(state.username))
                }
            } else {
                next(state)
            }
        }
    }
}
