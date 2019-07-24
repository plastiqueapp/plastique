package io.plastique.notifications

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
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
import io.plastique.core.snackbar.SnackbarState
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
import io.plastique.notifications.NotificationsEvent.LoadMoreStartedEvent
import io.plastique.notifications.NotificationsEvent.RefreshErrorEvent
import io.plastique.notifications.NotificationsEvent.RefreshEvent
import io.plastique.notifications.NotificationsEvent.RefreshFinishedEvent
import io.plastique.notifications.NotificationsEvent.RetryClickEvent
import io.plastique.notifications.NotificationsEvent.SnackbarShownEvent
import io.plastique.notifications.NotificationsEvent.UndoDeleteMessageEvent
import io.plastique.notifications.NotificationsEvent.UserChangedEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class NotificationsViewModel @Inject constructor(
    stateReducer: NotificationsStateReducer,
    effectHandlerFactory: NotificationsEffectHandlerFactory,
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
                contentState = ContentState.Empty,
                isSignedIn = signedIn,
                emptyState = EmptyState.MessageWithButton(
                    messageResId = R.string.notifications_message_sign_in_required,
                    buttonTextId = R.string.common_button_sign_in)))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: NotificationsEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<NotificationsEvent> {
        return sessionManager.userIdChanges
            .skip(1)
            .valveLatest(screenVisible)
            .map { userId -> UserChangedEvent(userId.toNullable()) }
    }

    companion object {
        private const val LOG_TAG = "NotificationsViewModel"
    }
}

@AutoFactory
class NotificationsEffectHandler(
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
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
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                notificationsModel.loadMore()
                    .toObservable<NotificationsEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
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
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<NotificationsEvent, NotificationsViewState, NotificationsEffect> {

    override fun reduce(state: NotificationsViewState, event: NotificationsEvent): StateWithEffects<NotificationsViewState, NotificationsEffect> =
        when (event) {
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
                        emptyState = EmptyState.Message(R.string.notifications_message_empty)))
                }
            }

            is LoadErrorEvent -> {
                next(state.copy(
                    contentState = ContentState.Empty,
                    listState = PagedListState.Empty,
                    emptyState = errorMessageProvider.getErrorState(event.error)))
            }

            RetryClickEvent -> {
                next(state.copy(contentState = ContentState.Loading, emptyState = null), LoadNotificationsEffect)
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

            is UserChangedEvent -> {
                val signedIn = event.userId != null
                if (signedIn != state.isSignedIn) {
                    if (signedIn) {
                        next(state.copy(contentState = ContentState.Loading, isSignedIn = signedIn, emptyState = null), LoadNotificationsEffect)
                    } else {
                        next(state.copy(
                            contentState = ContentState.Empty,
                            isSignedIn = signedIn,
                            listState = PagedListState.Empty,
                            emptyState = EmptyState.MessageWithButton(
                                messageResId = R.string.notifications_message_sign_in_required,
                                buttonTextId = R.string.common_button_sign_in)))
                    }
                } else {
                    next(state)
                }
            }

            is DeleteMessageEvent -> {
                next(state.copy(snackbarState = SnackbarState.MessageWithAction(
                    messageResId = R.string.notifications_message_notification_deleted,
                    actionTextId = R.string.common_button_undo,
                    actionData = event.messageId)),
                    DeleteMessageEffect(event.messageId))
            }

            is UndoDeleteMessageEvent -> {
                next(state, UndoDeleteMessageEffect(event.messageId))
            }
        }
}
