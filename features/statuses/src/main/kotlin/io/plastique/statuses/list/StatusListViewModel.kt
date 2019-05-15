package io.plastique.statuses.list

import android.text.TextUtils
import androidx.core.text.HtmlCompat
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
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class StatusListViewModel @Inject constructor(
    stateReducer: StatusListStateReducer,
    effectHandlerFactory: StatusListEffectHandlerFactory,
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
            sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) },
            contentSettings.showMatureChanges
                .valveLatest(screenVisible)
                .map { showMature -> ShowMatureChangedEvent(showMature) })
    }

    companion object {
        private const val LOG_TAG = "StatusListViewModel"
    }
}

@AutoFactory
class StatusListEffectHandler(
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
            .switchMapSingle {
                statusListModel.loadMore()
                    .toSingleDefault<StatusListEvent>(LoadMoreFinishedEvent)
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
    private val connectivityChecker: NetworkConnectivityChecker,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : StateReducer<StatusListEvent, StatusListViewState, StatusListEffect> {

    override fun reduce(state: StatusListViewState, event: StatusListEvent): StateWithEffects<StatusListViewState, StatusListEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyMessage = HtmlCompat.fromHtml(resourceProvider.getString(R.string.statuses_message_empty,
                    TextUtils.htmlEncode(state.params.username)), 0)
                ContentState.Empty(EmptyState.Message(emptyMessage))
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
            if (!state.isLoadingMore && connectivityChecker.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.statusItems + LoadingIndicatorItem), LoadMoreEffect)
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
                items = state.statusItems,
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
