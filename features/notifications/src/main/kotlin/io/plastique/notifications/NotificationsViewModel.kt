package io.plastique.notifications

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import com.sch.rxjava2.extensions.valveLatest
import io.plastique.core.BaseViewModel
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.inject.scopes.FragmentScope
import io.plastique.notifications.NotificationsEffect.DeleteMessageEffect
import io.plastique.notifications.NotificationsEffect.LoadMoreEffect
import io.plastique.notifications.NotificationsEffect.LoadNotificationsEffect
import io.plastique.notifications.NotificationsEffect.RefreshEffect
import io.plastique.notifications.NotificationsEffect.UndoDeleteMessageEffect
import io.plastique.notifications.NotificationsEvent.DeleteMessageEvent
import io.plastique.notifications.NotificationsEvent.ItemsChangedEvent
import io.plastique.notifications.NotificationsEvent.LoadErrorEvent
import io.plastique.notifications.NotificationsEvent.LoadMoreErrorEvent
import io.plastique.notifications.NotificationsEvent.LoadMoreEvent
import io.plastique.notifications.NotificationsEvent.LoadMoreFinishedEvent
import io.plastique.notifications.NotificationsEvent.RefreshErrorEvent
import io.plastique.notifications.NotificationsEvent.RefreshEvent
import io.plastique.notifications.NotificationsEvent.RefreshFinishedEvent
import io.plastique.notifications.NotificationsEvent.RetryClickEvent
import io.plastique.notifications.NotificationsEvent.SessionChangedEvent
import io.plastique.notifications.NotificationsEvent.SnackbarShownEvent
import io.plastique.notifications.NotificationsEvent.UndoDeleteMessageEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class NotificationsViewModel @Inject constructor(
    stateReducer: NotificationsStateReducer,
    effectHandlerFactory: NotificationsEffectHandlerFactory,
    private val resourceProvider: ResourceProvider,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    lateinit var state: Observable<NotificationsViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = effectHandlerFactory.create(screenVisible),
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init() {
        if (::state.isInitialized) return

        val signedIn = sessionManager.session is Session.User
        val stateAndEffects = if (signedIn) {
            next(NotificationsViewState(
                    contentState = ContentState.Loading,
                    isSignedIn = signedIn),
                    LoadNotificationsEffect)
        } else {
            next(NotificationsViewState(
                    contentState = ContentState.Empty(EmptyState.MessageWithButton(
                            message = resourceProvider.getString(R.string.notifications_message_sign_in_required),
                            button = resourceProvider.getString(R.string.common_button_sign_in))),
                    isSignedIn = signedIn))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: NotificationsEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<NotificationsEvent> {
        return sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) }
    }

    companion object {
        private const val LOG_TAG = "NotificationsViewModel"
    }
}

@AutoFactory
class NotificationsEffectHandler(
    @Provided private val notificationsModel: NotificationsModel,
    @Provided private val sessionManager: SessionManager,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<NotificationsEffect, NotificationsEvent> {

    override fun handle(effects: Observable<NotificationsEffect>): Observable<NotificationsEvent> {
        val itemEvents = effects.ofType<LoadNotificationsEffect>()
                .switchMap {
                    notificationsModel.items()
                            .valveLatest(screenVisible)
                            .takeWhile { sessionManager.session is Session.User }
                            .map<NotificationsEvent> { itemsData -> ItemsChangedEvent(itemsData.items, itemsData.hasMore) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .switchMapSingle {
                    notificationsModel.loadMore()
                            .toSingleDefault<NotificationsEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(error) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .switchMapSingle {
                    notificationsModel.refresh()
                            .toSingleDefault<NotificationsEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(error) }
                }

        val deleteEvents = effects.ofType<DeleteMessageEffect>()
                .flatMapCompletable { effect -> notificationsModel.deleteMessageById(effect.messageId) }

        val undoDeleteEvents = effects.ofType<UndoDeleteMessageEffect>()
                .flatMapCompletable { effect -> notificationsModel.undoDeleteMessageById(effect.messageId) }

        return Observable.mergeArray(itemEvents, loadMoreEvents, refreshEvents, deleteEvents.toObservable(), undoDeleteEvents.toObservable())
    }
}

class NotificationsStateReducer @Inject constructor(
    private val connectivityChecker: NetworkConnectivityChecker,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : StateReducer<NotificationsEvent, NotificationsViewState, NotificationsEffect> {

    override fun reduce(state: NotificationsViewState, event: NotificationsEvent): StateWithEffects<NotificationsViewState, NotificationsEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                ContentState.Empty(EmptyState.Message(resourceProvider.getString(R.string.notifications_message_empty)))
            }
            next(state.copy(
                    contentState = contentState,
                    items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                    contentItems = event.items,
                    hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                    contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error)),
                    items = emptyList(),
                    contentItems = emptyList(),
                    hasMore = false))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadNotificationsEffect)
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityChecker.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.contentItems + LoadingIndicatorItem), LoadMoreEffect)
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
                    items = state.contentItems,
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

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is SessionChangedEvent -> {
            val signedIn = event.session is Session.User
            if (signedIn != state.isSignedIn) {
                if (signedIn) {
                    next(state.copy(contentState = ContentState.Loading, isSignedIn = signedIn), LoadNotificationsEffect)
                } else {
                    next(state.copy(
                            contentState = ContentState.Empty(EmptyState.MessageWithButton(
                                    message = resourceProvider.getString(R.string.notifications_message_sign_in_required),
                                    button = resourceProvider.getString(R.string.common_button_sign_in))),
                            isSignedIn = signedIn))
                }
            } else {
                next(state)
            }
        }

        is DeleteMessageEvent -> {
            next(state.copy(snackbarState = SnackbarState.MessageWithAction(
                    message = resourceProvider.getString(R.string.notifications_message_notification_deleted),
                    actionText = resourceProvider.getString(R.string.common_button_undo),
                    actionData = event.messageId)),
                    DeleteMessageEffect(event.messageId))
        }

        is UndoDeleteMessageEvent -> {
            next(state, UndoDeleteMessageEffect(event.messageId))
        }
    }
}
