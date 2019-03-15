package io.plastique.deviations.list

import io.plastique.collections.FavoritesModel
import io.plastique.core.BaseViewModel
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.exceptions.NoNetworkConnectionException
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.deviations.FetchParams
import io.plastique.deviations.HotParams
import io.plastique.deviations.PopularParams
import io.plastique.deviations.R
import io.plastique.deviations.UndiscoveredParams
import io.plastique.deviations.list.DeviationListEffect.LoadDeviationsEffect
import io.plastique.deviations.list.DeviationListEffect.LoadMoreEffect
import io.plastique.deviations.list.DeviationListEffect.RefreshEffect
import io.plastique.deviations.list.DeviationListEffect.SetFavoriteEffect
import io.plastique.deviations.list.DeviationListEvent.ConnectionStateChangedEvent
import io.plastique.deviations.list.DeviationListEvent.ItemsChangedEvent
import io.plastique.deviations.list.DeviationListEvent.LayoutModeChangedEvent
import io.plastique.deviations.list.DeviationListEvent.LoadErrorEvent
import io.plastique.deviations.list.DeviationListEvent.LoadMoreErrorEvent
import io.plastique.deviations.list.DeviationListEvent.LoadMoreEvent
import io.plastique.deviations.list.DeviationListEvent.LoadMoreFinishedEvent
import io.plastique.deviations.list.DeviationListEvent.ParamsChangedEvent
import io.plastique.deviations.list.DeviationListEvent.RefreshErrorEvent
import io.plastique.deviations.list.DeviationListEvent.RefreshEvent
import io.plastique.deviations.list.DeviationListEvent.RefreshFinishedEvent
import io.plastique.deviations.list.DeviationListEvent.RetryClickEvent
import io.plastique.deviations.list.DeviationListEvent.SetFavoriteErrorEvent
import io.plastique.deviations.list.DeviationListEvent.SetFavoriteEvent
import io.plastique.deviations.list.DeviationListEvent.SetFavoriteFinishedEvent
import io.plastique.deviations.list.DeviationListEvent.ShowLiteratureChangedEvent
import io.plastique.deviations.list.DeviationListEvent.ShowMatureChangedEvent
import io.plastique.deviations.list.DeviationListEvent.SnackbarShownEvent
import io.plastique.deviations.tags.Tag
import io.plastique.deviations.tags.TagFactory
import io.plastique.inject.scopes.FragmentScope
import io.plastique.util.NetworkConnectionState
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class DeviationListViewModel @Inject constructor(
    stateReducer: DeviationListStateReducer,
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val contentSettings: ContentSettings,
    private val deviationListModel: DeviationListModel,
    private val favoritesModel: FavoritesModel,
    private val tagFactory: TagFactory
) : BaseViewModel() {

    lateinit var state: Observable<DeviationListViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(params: FetchParams) {
        if (::state.isInitialized) return

        val actualParams = params.with(
                showLiterature = contentSettings.showLiterature,
                showMatureContent = contentSettings.showMature)
        val initialState = DeviationListViewState(
                layoutMode = contentSettings.layoutMode,
                params = actualParams,
                tags = createTags(tagFactory, actualParams),
                contentState = ContentState.Loading)

        state = loop.loop(initialState, LoadDeviationsEffect(actualParams)).disposeOnDestroy()
    }

    fun dispatch(event: DeviationListEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<DeviationListEffect>): Observable<DeviationListEvent> {
        val loadEvents = effects.ofType<LoadDeviationsEffect>()
                .switchMap { effect ->
                    deviationListModel.getItems(effect.params)
                            .bindToLifecycle()
                            .map<DeviationListEvent> { data -> ItemsChangedEvent(items = data.items, hasMore = data.hasMore) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }

        // TODO: Cancel on new LoadDeviationsEffect
        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .switchMapSingle {
                    deviationListModel.loadMore()
                            .toSingleDefault<DeviationListEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(error) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .switchMapSingle {
                    deviationListModel.refresh()
                            .toSingleDefault<DeviationListEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(error) }
                }

        val favoriteEvents = effects.ofType<SetFavoriteEffect>()
                .flatMapSingle { effect ->
                    favoritesModel.setFavorite(effect.deviationId, effect.favorite)
                            .toSingleDefault<DeviationListEvent>(SetFavoriteFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> SetFavoriteErrorEvent(error) }
                }

        return Observable.merge(loadEvents, loadMoreEvents, refreshEvents, favoriteEvents)
    }

    private fun externalEvents(): Observable<DeviationListEvent> {
        return Observable.merge(
                connectivityMonitor.connectionState
                        .bindToLifecycle()
                        .map { connectionState -> ConnectionStateChangedEvent(connectionState) },
                contentSettings.showLiteratureChanges
                        .bindToLifecycle()
                        .map { showLiterature -> ShowLiteratureChangedEvent(showLiterature) },
                contentSettings.showMatureChanges
                        .bindToLifecycle()
                        .map { showMature -> ShowMatureChangedEvent(showMature) },
                contentSettings.layoutModeChanges
                        .bindToLifecycle()
                        .map { layoutMode -> LayoutModeChangedEvent(layoutMode) }
        )
    }

    companion object {
        private const val LOG_TAG = "DeviationListViewModel"
    }
}

class DeviationListStateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider,
    private val tagFactory: TagFactory
) : Reducer<DeviationListEvent, DeviationListViewState, DeviationListEffect> {
    override fun invoke(state: DeviationListViewState, event: DeviationListEvent): Next<DeviationListViewState, DeviationListEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isEmpty()) {
                ContentState.Empty(EmptyState(message = resourceProvider.getString(R.string.deviations_message_empty)))
            } else {
                ContentState.Content
            }
            next(state.copy(
                    contentState = contentState,
                    items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                    hasMore = event.hasMore,
                    deviationItems = event.items))
        }

        is LoadErrorEvent -> {
            val errorState = errorMessageProvider.getErrorState(event.error, R.string.deviations_message_load_error)
            next(state.copy(
                    contentState = ContentState.Empty(isError = true, error = event.error, emptyState = errorState),
                    items = emptyList(),
                    deviationItems = emptyList()))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadDeviationsEffect(state.params))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.deviationItems + LoadingIndicatorItem), LoadMoreEffect)
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
                    items = state.deviationItems,
                    snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.deviations_message_load_error))))
        }

        RefreshEvent -> {
            next(state.copy(isRefreshing = true), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(isRefreshing = false))
        }

        is RefreshErrorEvent -> {
            next(state.copy(isRefreshing = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.deviations_message_load_error))))
        }

        is ParamsChangedEvent -> {
            onFilterChanged(state, event.params)
        }

        is ShowLiteratureChangedEvent -> {
            onFilterChanged(state, state.params.with(showLiterature = event.showLiterature))
        }

        is ShowMatureChangedEvent -> {
            onFilterChanged(state, state.params.with(showMatureContent = event.showMature))
        }

        is LayoutModeChangedEvent -> {
            next(state.copy(layoutMode = event.layoutMode))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is ConnectionStateChangedEvent -> {
            if (event.connectionState === NetworkConnectionState.Connected &&
                    state.contentState is ContentState.Empty &&
                    state.contentState.error is NoNetworkConnectionException) {
                next(state.copy(
                        contentState = ContentState.Loading),
                        LoadDeviationsEffect(state.params))
            } else {
                next(state)
            }
        }

        is SetFavoriteEvent -> {
            next(state.copy(showProgressDialog = true), SetFavoriteEffect(event.deviationId, event.favorite))
        }

        SetFavoriteFinishedEvent -> {
            next(state.copy(showProgressDialog = false))
        }

        is SetFavoriteErrorEvent -> {
            val errorMessage = errorMessageProvider.getErrorMessage(event.error)
            next(state.copy(showProgressDialog = false, snackbarState = SnackbarState.Message(errorMessage)))
        }
    }

    private fun onFilterChanged(state: DeviationListViewState, params: FetchParams): Next<DeviationListViewState, DeviationListEffect> {
        return if (state.params != params) {
            next(state.copy(
                    params = params,
                    contentState = ContentState.Loading,
                    items = emptyList(),
                    deviationItems = emptyList(),
                    tags = createTags(tagFactory, params)),
                    LoadDeviationsEffect(params))
        } else {
            next(state)
        }
    }
}

private fun createTags(tagFactory: TagFactory, params: FetchParams): List<Tag> = when (params) {
    is HotParams -> tagFactory.createCategoryTags(params.category)
    is PopularParams -> tagFactory.createCategoryTags(params.category) + tagFactory.createTimeRangeTag(params.timeRange)
    is UndiscoveredParams -> tagFactory.createCategoryTags(params.category)
    else -> emptyList()
}
