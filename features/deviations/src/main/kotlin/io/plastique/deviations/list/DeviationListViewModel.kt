package io.plastique.deviations.list

import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.deviations.ContentSettings
import io.plastique.deviations.DailyParams
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationDataSource
import io.plastique.deviations.FetchParams
import io.plastique.deviations.HotParams
import io.plastique.deviations.PopularParams
import io.plastique.deviations.R
import io.plastique.deviations.UndiscoveredParams
import io.plastique.deviations.list.DeviationListEffect.LoadDeviationsEffect
import io.plastique.deviations.list.DeviationListEffect.LoadMoreEffect
import io.plastique.deviations.list.DeviationListEffect.RefreshEffect
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
import io.plastique.deviations.list.DeviationListEvent.ShowLiteratureChangedEvent
import io.plastique.deviations.list.DeviationListEvent.ShowMatureChangedEvent
import io.plastique.deviations.list.DeviationListEvent.SnackbarShown
import io.plastique.deviations.tags.Tag
import io.plastique.deviations.tags.TagFactory
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import org.threeten.bp.LocalDate
import timber.log.Timber
import javax.inject.Inject

class DeviationListViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val contentSettings: ContentSettings,
    private val dataSource: DeviationDataSource,
    private val tagFactory: TagFactory,
    private val errorMessageProvider: ErrorMessageProvider
) : ViewModel() {

    lateinit var state: Observable<DeviationListViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(params: FetchParams) {
        if (::state.isInitialized) return

        val layoutMode = contentSettings.layoutMode
        val newParams = params.with(
                showLiterature = contentSettings.showLiterature,
                showMatureContent = contentSettings.showMature)
        val initialState = DeviationListViewState(
                layoutMode = layoutMode,
                params = newParams,
                tags = createTags(tagFactory, params))

        state = loop.loop(initialState, LoadDeviationsEffect(newParams)).disposeOnDestroy()
    }

    fun dispatch(event: DeviationListEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<DeviationListEffect>): Observable<DeviationListEvent> {
        val loadEvents = effects.ofType<LoadDeviationsEffect>()
                .switchMap { effect ->
                    dataSource.getData(effect.params)
                            .bindToLifecycle()
                            .map<DeviationListEvent> { data ->
                                ItemsChangedEvent(items = createItems(data.value, effect.params is DailyParams), hasMore = data.hasMore)
                            }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(errorMessageProvider.getErrorState(error, R.string.deviations_message_load_error)) }
                }

        // TODO: Cancel on new LoadDeviationsEffect
        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .switchMapSingle {
                    dataSource.loadMore()
                            .toSingleDefault<DeviationListEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(errorMessageProvider.getErrorMessage(error, R.string.deviations_message_load_error)) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .switchMapSingle {
                    dataSource.refresh()
                            .toSingleDefault<DeviationListEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(errorMessageProvider.getErrorMessage(error, R.string.deviations_message_load_error)) }
                }

        return Observable.merge(loadEvents, loadMoreEvents, refreshEvents)
    }

    private fun externalEvents(): Observable<DeviationListEvent> {
        return Observable.merge(
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

class StateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
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
                    items = if (state.loadingMore) event.items + LoadingIndicatorItem else event.items,
                    hasMore = event.hasMore,
                    deviationItems = event.items))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                    contentState = ContentState.Empty(event.emptyState, isError = true),
                    items = emptyList(),
                    deviationItems = emptyList()))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadDeviationsEffect(state.params))
        }

        LoadMoreEvent -> {
            if (!state.loadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(loadingMore = true, items = state.deviationItems + LoadingIndicatorItem), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(loadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(loadingMore = false, items = state.deviationItems, snackbarMessage = event.errorMessage))
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

        SnackbarShown -> {
            next(state.copy(snackbarMessage = null))
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

private fun createItems(deviations: List<Deviation>, daily: Boolean): List<ListItem> {
    var index = 0
    return if (daily) {
        val items = ArrayList<ListItem>(deviations.size + 1)
        var prevDate: LocalDate? = null
        for (deviation in deviations) {
            val date = deviation.dailyDeviation!!.date.toLocalDate()
            if (date != prevDate) {
                items.add(DateItem(date))
                prevDate = date
                index = 0
            }
            items.add(DeviationItem(deviation).also { it.index = index++ })
        }
        items
    } else {
        deviations.map { deviation -> DeviationItem(deviation).also { it.index = index++ } }
    }
}
