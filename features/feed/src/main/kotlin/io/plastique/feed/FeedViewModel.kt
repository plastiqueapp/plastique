package io.plastique.feed

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
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.lists.PagedListState
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.plastique.feed.FeedEffect.LoadFeedEffect
import io.plastique.feed.FeedEffect.LoadMoreEffect
import io.plastique.feed.FeedEffect.OpenSignInEffect
import io.plastique.feed.FeedEffect.RefreshEffect
import io.plastique.feed.FeedEffect.SetFavoriteEffect
import io.plastique.feed.FeedEffect.SetFeedSettingsEffect
import io.plastique.feed.FeedEvent.ItemsChangedEvent
import io.plastique.feed.FeedEvent.LoadErrorEvent
import io.plastique.feed.FeedEvent.LoadMoreErrorEvent
import io.plastique.feed.FeedEvent.LoadMoreEvent
import io.plastique.feed.FeedEvent.LoadMoreFinishedEvent
import io.plastique.feed.FeedEvent.LoadMoreStartedEvent
import io.plastique.feed.FeedEvent.RefreshErrorEvent
import io.plastique.feed.FeedEvent.RefreshEvent
import io.plastique.feed.FeedEvent.RefreshFinishedEvent
import io.plastique.feed.FeedEvent.RetryClickEvent
import io.plastique.feed.FeedEvent.SetFavoriteErrorEvent
import io.plastique.feed.FeedEvent.SetFavoriteEvent
import io.plastique.feed.FeedEvent.SetFavoriteFinishedEvent
import io.plastique.feed.FeedEvent.SetFeedSettingsEvent
import io.plastique.feed.FeedEvent.SettingsChangeErrorEvent
import io.plastique.feed.FeedEvent.SettingsChangedEvent
import io.plastique.feed.FeedEvent.ShowMatureChangedEvent
import io.plastique.feed.FeedEvent.SnackbarShownEvent
import io.plastique.feed.FeedEvent.UserChangedEvent
import io.plastique.feed.settings.FeedSettingsManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class FeedViewModel @Inject constructor(
    stateReducer: FeedStateReducer,
    effectHandlerFactory: FeedEffectHandlerFactory,
    val navigator: FeedNavigator,
    private val contentSettings: ContentSettings,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(navigator, screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    val state: Observable<FeedViewState> by lazy(LazyThreadSafetyMode.NONE) {
        val showMatureContent = contentSettings.showMature
        val signedIn = sessionManager.session is Session.User
        val stateAndEffects = if (signedIn) {
            next(FeedViewState(
                contentState = ContentState.Loading,
                isSignedIn = signedIn,
                showMatureContent = showMatureContent),
                LoadFeedEffect(showMatureContent))
        } else {
            next(FeedViewState(contentState = ContentState.Empty,
                isSignedIn = signedIn,
                showMatureContent = showMatureContent,
                emptyState = EmptyState.MessageWithButton(
                    messageResId = R.string.feed_message_sign_in,
                    buttonTextId = R.string.common_button_sign_in)))
        }

        loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: FeedEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<FeedEvent> {
        return Observable.merge(
            contentSettings.showMatureChanges
                .valveLatest(screenVisible)
                .map { showMature -> ShowMatureChangedEvent(showMature) },
            sessionManager.userIdChanges
                .skip(1)
                .valveLatest(screenVisible)
                .map { userId -> UserChangedEvent(userId.toNullable()) })
    }

    private companion object {
        private const val LOG_TAG = "FeedViewModel"
    }
}

@AutoFactory
class FeedEffectHandler(
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
    @Provided private val feedModel: FeedModel,
    @Provided private val feedSettingsManager: FeedSettingsManager,
    @Provided private val favoritesModel: FavoritesModel,
    @Provided private val sessionManager: SessionManager,
    private val navigator: FeedNavigator,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<FeedEffect, FeedEvent> {

    override fun handle(effects: Observable<FeedEffect>): Observable<FeedEvent> {
        val loadEvents = effects.ofType<LoadFeedEffect>()
            .switchMap { effect ->
                feedModel.items(effect.matureContent)
                    .valveLatest(screenVisible)
                    .takeWhile { sessionManager.session is Session.User }
                    .map<FeedEvent> { itemsData -> ItemsChangedEvent(itemsData.items, itemsData.hasMore) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                feedModel.loadMore()
                    .toObservable<FeedEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadMoreErrorEvent(error) }
            }

        val refreshEvents = effects.ofType<RefreshEffect>()
            .switchMapSingle {
                feedModel.refresh()
                    .toSingleDefault<FeedEvent>(RefreshFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> RefreshErrorEvent(error) }
            }

        val settingsEvents = effects.ofType<SetFeedSettingsEffect>()
            .switchMapSingle { effect ->
                feedSettingsManager.updateSettings(effect.settings)
                    .toSingleDefault<FeedEvent>(SettingsChangedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> SettingsChangeErrorEvent(error) }
            }

        val favoriteEvents = effects.ofType<SetFavoriteEffect>()
            .flatMapSingle { effect ->
                favoritesModel.setFavorite(effect.deviationId, effect.favorite)
                    .toSingleDefault<FeedEvent>(SetFavoriteFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> SetFavoriteErrorEvent(error) }
            }

        val navigationEvents = effects.ofType<OpenSignInEffect>()
            .doOnNext { navigator.openSignIn() }
            .ignoreElements()
            .toObservable<FeedEvent>()

        return Observable.mergeArray(loadEvents, loadMoreEvents, refreshEvents, settingsEvents, favoriteEvents, navigationEvents)
    }
}

class FeedStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<FeedEvent, FeedViewState, FeedEffect> {

    override fun reduce(state: FeedViewState, event: FeedEvent): StateWithEffects<FeedViewState, FeedEffect> = when (event) {
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
                    emptyState = EmptyState.Message(R.string.feed_message_empty)))
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
            if (state.isSignedIn) {
                next(state.copy(contentState = ContentState.Loading, emptyState = null), LoadFeedEffect(state.showMatureContent))
            } else {
                next(state, OpenSignInEffect)
            }
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = null))
        }

        is UserChangedEvent -> {
            val signedIn = event.userId != null
            if (signedIn != state.isSignedIn) {
                if (signedIn) {
                    next(state.copy(
                        contentState = ContentState.Loading,
                        isSignedIn = signedIn,
                        emptyState = null),
                        LoadFeedEffect(state.showMatureContent))
                } else {
                    next(state.copy(
                        contentState = ContentState.Empty,
                        isSignedIn = signedIn,
                        listState = PagedListState.Empty,
                        emptyState = EmptyState.MessageWithButton(
                            messageResId = R.string.feed_message_sign_in,
                            buttonTextId = R.string.common_button_sign_in)))
                }
            } else {
                next(state)
            }
        }

        is ShowMatureChangedEvent -> {
            if (state.showMatureContent != event.showMatureContent) {
                next(state.copy(
                    contentState = ContentState.Loading,
                    listState = PagedListState.Empty,
                    showMatureContent = event.showMatureContent,
                    emptyState = null),
                    LoadFeedEffect(event.showMatureContent))
            } else {
                next(state)
            }
        }

        is SetFeedSettingsEvent -> {
            next(state.copy(isApplyingSettings = true), SetFeedSettingsEffect(event.settings))
        }

        SettingsChangedEvent -> {
            next(state.copy(
                isApplyingSettings = false,
                contentState = ContentState.Loading,
                listState = PagedListState.Empty,
                emptyState = null),
                LoadFeedEffect(state.showMatureContent))
        }

        is SettingsChangeErrorEvent -> {
            next(state.copy(
                isApplyingSettings = false,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.feed_message_settings_change_error))))
        }

        is SetFavoriteEvent -> {
            next(state.copy(showProgressDialog = true), SetFavoriteEffect(event.deviationId, event.favorite))
        }

        SetFavoriteFinishedEvent -> {
            next(state.copy(showProgressDialog = false))
        }

        is SetFavoriteErrorEvent -> {
            next(state.copy(
                showProgressDialog = false,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
        }
    }
}
