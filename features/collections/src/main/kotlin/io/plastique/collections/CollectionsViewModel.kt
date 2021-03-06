package io.plastique.collections

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
import io.plastique.collections.CollectionsEffect.CreateFolderEffect
import io.plastique.collections.CollectionsEffect.DeleteFolderEffect
import io.plastique.collections.CollectionsEffect.LoadCollectionsEffect
import io.plastique.collections.CollectionsEffect.LoadMoreEffect
import io.plastique.collections.CollectionsEffect.OpenSignInEffect
import io.plastique.collections.CollectionsEffect.RefreshEffect
import io.plastique.collections.CollectionsEffect.UndoDeleteFolderEffect
import io.plastique.collections.CollectionsEvent.CreateFolderErrorEvent
import io.plastique.collections.CollectionsEvent.CreateFolderEvent
import io.plastique.collections.CollectionsEvent.DeleteFolderEvent
import io.plastique.collections.CollectionsEvent.FolderCreatedEvent
import io.plastique.collections.CollectionsEvent.FolderDeletedEvent
import io.plastique.collections.CollectionsEvent.ItemsChangedEvent
import io.plastique.collections.CollectionsEvent.LoadErrorEvent
import io.plastique.collections.CollectionsEvent.LoadMoreErrorEvent
import io.plastique.collections.CollectionsEvent.LoadMoreEvent
import io.plastique.collections.CollectionsEvent.LoadMoreFinishedEvent
import io.plastique.collections.CollectionsEvent.LoadMoreStartedEvent
import io.plastique.collections.CollectionsEvent.RefreshErrorEvent
import io.plastique.collections.CollectionsEvent.RefreshEvent
import io.plastique.collections.CollectionsEvent.RefreshFinishedEvent
import io.plastique.collections.CollectionsEvent.RetryClickEvent
import io.plastique.collections.CollectionsEvent.ShowMatureChangedEvent
import io.plastique.collections.CollectionsEvent.SnackbarShownEvent
import io.plastique.collections.CollectionsEvent.UndoDeleteFolderEvent
import io.plastique.collections.CollectionsEvent.UserChangedEvent
import io.plastique.collections.folders.FolderLoadParams
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
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class CollectionsViewModel @Inject constructor(
    stateReducer: CollectionsStateReducer,
    effectHandlerFactory: CollectionsEffectHandlerFactory,
    val navigator: CollectionsNavigator,
    private val sessionManager: SessionManager,
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    lateinit var state: Observable<CollectionsViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(navigator, screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String?) {
        if (::state.isInitialized) return

        val params = FolderLoadParams(username = username, matureContent = contentSettings.showMature)
        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(CollectionsViewState(
                params = params,
                contentState = ContentState.Empty,
                signInNeeded = signInNeeded,
                emptyState = EmptyState.MessageWithButton(
                    messageResId = R.string.collections_message_sign_in,
                    buttonTextId = R.string.common_button_sign_in)))
        } else {
            next(CollectionsViewState(
                params = params,
                contentState = ContentState.Loading,
                signInNeeded = signInNeeded),
                LoadCollectionsEffect(params))
        }

        state = loop.loop(stateAndEffects).disposeOnDestroy()
    }

    fun dispatch(event: CollectionsEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<CollectionsEvent> {
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
        private const val LOG_TAG = "CollectionsViewModel"
    }
}

@AutoFactory
class CollectionsEffectHandler(
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
    @Provided private val dataSource: FoldersWithDeviationsDataSource,
    @Provided private val collectionsModel: CollectionsModel,
    private val navigator: CollectionsNavigator,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<CollectionsEffect, CollectionsEvent> {

    override fun handle(effects: Observable<CollectionsEffect>): Observable<CollectionsEvent> {
        val loadEvents = effects.ofType<LoadCollectionsEffect>()
            .switchMap { effect ->
                dataSource.items(effect.params)
                    .valveLatest(screenVisible)
                    .map<CollectionsEvent> { pagedData -> ItemsChangedEvent(items = pagedData.items, hasMore = pagedData.hasMore) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                dataSource.loadMore()
                    .toObservable<CollectionsEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadMoreErrorEvent(error) }
            }

        val refreshEvents = effects.ofType<RefreshEffect>()
            .switchMapSingle {
                dataSource.refresh()
                    .toSingleDefault<CollectionsEvent>(RefreshFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> RefreshErrorEvent(error) }
            }

        val createFolderEvents = effects.ofType<CreateFolderEffect>()
            .flatMapSingle { effect ->
                collectionsModel.createFolder(effect.folderName)
                    .toSingleDefault<CollectionsEvent>(FolderCreatedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> CreateFolderErrorEvent(error) }
            }

        val deleteFolderEvents = effects.ofType<DeleteFolderEffect>()
            .flatMapSingle { effect ->
                collectionsModel.deleteFolderById(effect.folderId)
                    .toSingleDefault(FolderDeletedEvent(effect.folderId, effect.folderName))
            }

        val undoDeleteFolderEvents = effects.ofType<UndoDeleteFolderEffect>()
            .flatMapCompletable { effect -> collectionsModel.undoDeleteFolderById(effect.folderId) }
            .toObservable<CollectionsEvent>()

        val navigationEvents = effects.ofType<OpenSignInEffect>()
            .doOnNext { navigator.openSignIn() }
            .ignoreElements()
            .toObservable<CollectionsEvent>()

        return Observable.mergeArray(loadEvents, loadMoreEvents, refreshEvents, createFolderEvents, deleteFolderEvents, undoDeleteFolderEvents,
            navigationEvents)
    }
}

class CollectionsStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<CollectionsEvent, CollectionsViewState, CollectionsEffect> {

    override fun reduce(state: CollectionsViewState, event: CollectionsEvent): StateWithEffects<CollectionsViewState, CollectionsEffect> = when (event) {
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
                val emptyState = if (state.params.username != null) {
                    EmptyState.Message(R.string.collections_message_empty_user_collection, listOf(state.params.username.htmlEncode()))
                } else {
                    EmptyState.Message(R.string.collections_message_empty_collection)
                }
                next(state.copy(
                    contentState = ContentState.Empty,
                    listState = PagedListState.Empty,
                    emptyState = emptyState))
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
            if (!state.signInNeeded) {
                next(state.copy(contentState = ContentState.Loading, emptyState = null), LoadCollectionsEffect(state.params))
            } else {
                next(state, OpenSignInEffect)
            }
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = null))
        }

        is UserChangedEvent -> {
            val signInNeeded = state.params.username == null && event.userId == null
            if (signInNeeded != state.signInNeeded) {
                if (signInNeeded) {
                    next(state.copy(
                        contentState = ContentState.Empty,
                        signInNeeded = signInNeeded,
                        listState = PagedListState.Empty,
                        emptyState = EmptyState.MessageWithButton(
                            messageResId = R.string.collections_message_sign_in,
                            buttonTextId = R.string.common_button_sign_in)))
                } else {
                    next(state.copy(
                        contentState = ContentState.Loading,
                        signInNeeded = signInNeeded,
                        listState = PagedListState.Empty,
                        emptyState = null),
                        LoadCollectionsEffect(state.params))
                }
            } else {
                next(state)
            }
        }

        is ShowMatureChangedEvent -> {
            if (state.params.matureContent != event.showMature) {
                val params = state.params.copy(matureContent = event.showMature)
                next(state.copy(
                    params = params,
                    contentState = ContentState.Loading,
                    listState = PagedListState.Empty,
                    emptyState = null),
                    LoadCollectionsEffect(params))
            } else {
                next(state)
            }
        }

        is CreateFolderEvent -> {
            next(state.copy(showProgressDialog = true), CreateFolderEffect(event.folderName))
        }

        FolderCreatedEvent -> {
            next(state.copy(showProgressDialog = false))
        }

        is CreateFolderErrorEvent -> {
            next(state.copy(
                showProgressDialog = false,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.collections_message_folder_create_error))))
        }

        is DeleteFolderEvent -> {
            next(state, DeleteFolderEffect(event.folderId, event.folderName))
        }

        is FolderDeletedEvent -> {
            next(state.copy(snackbarState = SnackbarState.MessageWithAction(
                messageResId = R.string.collections_message_folder_deleted,
                messageArgs = listOf(event.folderName.htmlEncode()),
                actionTextId = R.string.common_button_undo,
                actionData = event.folderId)))
        }

        is UndoDeleteFolderEvent -> {
            next(state, UndoDeleteFolderEffect(event.folderId))
        }
    }
}
