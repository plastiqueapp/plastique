package io.plastique.deviations.list

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
import io.plastique.collections.FavoritesModel
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
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.deviations.DeviationsNavigator
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
import io.plastique.deviations.list.DeviationListEvent.LoadMoreStartedEvent
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
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class DeviationListViewModel @Inject constructor(
    stateReducer: DeviationListStateReducer,
    effectHandlerFactory: DeviationListEffectHandlerFactory,
    val navigator: DeviationsNavigator,
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val contentSettings: ContentSettings,
    private val tagFactory: TagFactory
) : BaseViewModel() {

    lateinit var state: Observable<DeviationListViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
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

    private fun externalEvents(): Observable<DeviationListEvent> {
        return Observable.merge(
            connectivityMonitor.connectionState
                .valveLatest(screenVisible)
                .map { connectionState -> ConnectionStateChangedEvent(connectionState) },
            contentSettings.showLiteratureChanges
                .valveLatest(screenVisible)
                .map { showLiterature -> ShowLiteratureChangedEvent(showLiterature) },
            contentSettings.showMatureChanges
                .valveLatest(screenVisible)
                .map { showMature -> ShowMatureChangedEvent(showMature) },
            contentSettings.layoutModeChanges
                .valveLatest(screenVisible)
                .map { layoutMode -> LayoutModeChangedEvent(layoutMode) })
    }

    companion object {
        private const val LOG_TAG = "DeviationListViewModel"
    }
}

@AutoFactory
class DeviationListEffectHandler(
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
    @Provided private val deviationListModel: DeviationListModel,
    @Provided private val favoritesModel: FavoritesModel,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<DeviationListEffect, DeviationListEvent> {

    override fun handle(effects: Observable<DeviationListEffect>): Observable<DeviationListEvent> {
        val loadEvents = effects.ofType<LoadDeviationsEffect>()
            .switchMap { effect ->
                deviationListModel.getItems(effect.params)
                    .valveLatest(screenVisible)
                    .map<DeviationListEvent> { data -> ItemsChangedEvent(items = data.items, hasMore = data.hasMore) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        // TODO: Cancel on new LoadDeviationsEffect
        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                deviationListModel.loadMore()
                    .toObservable<DeviationListEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
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
}

class DeviationListStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider,
    private val tagFactory: TagFactory
) : StateReducer<DeviationListEvent, DeviationListViewState, DeviationListEffect> {

    override fun reduce(state: DeviationListViewState, event: DeviationListEvent): StateWithEffects<DeviationListViewState, DeviationListEffect> =
        when (event) {
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
                    next(state.copy(
                        contentState = ContentState.Empty,
                        errorType = ErrorType.None,
                        listState = PagedListState.Empty,
                        emptyState = EmptyState.Message(R.string.deviations_message_empty)))
                }
            }

            is LoadErrorEvent -> {
                next(state.copy(
                    contentState = ContentState.Empty,
                    errorType = event.error.toErrorType(),
                    emptyState = errorMessageProvider.getErrorState(event.error, R.string.deviations_message_load_error),
                    listState = PagedListState.Empty))
            }

            RetryClickEvent -> {
                next(state.copy(contentState = ContentState.Loading, errorType = ErrorType.None, emptyState = null), LoadDeviationsEffect(state.params))
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
                    snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.deviations_message_load_error))))
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
                    snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.deviations_message_load_error))))
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
                next(state.copy(snackbarState = null))
            }

            is ConnectionStateChangedEvent -> {
                if (event.connectionState == NetworkConnectionState.Connected &&
                    state.contentState == ContentState.Empty &&
                    state.errorType == ErrorType.NoNetworkConnection) {
                    next(state.copy(contentState = ContentState.Loading, errorType = ErrorType.None, emptyState = null), LoadDeviationsEffect(state.params))
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
                next(state.copy(showProgressDialog = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
            }
        }

    private fun onFilterChanged(state: DeviationListViewState, params: FetchParams): StateWithEffects<DeviationListViewState, DeviationListEffect> {
        return if (state.params != params) {
            next(state.copy(
                params = params,
                contentState = ContentState.Loading,
                errorType = ErrorType.None,
                listState = PagedListState.Empty,
                tags = createTags(tagFactory, params),
                emptyState = null),
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
