package io.plastique.collections

import androidx.core.text.htmlEncode
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import com.sch.rxjava2.extensions.valveLatest
import io.plastique.collections.CollectionsEffect.CreateFolderEffect
import io.plastique.collections.CollectionsEffect.DeleteFolderEffect
import io.plastique.collections.CollectionsEffect.LoadCollectionsEffect
import io.plastique.collections.CollectionsEffect.LoadMoreEffect
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
import io.plastique.collections.CollectionsEvent.RefreshErrorEvent
import io.plastique.collections.CollectionsEvent.RefreshEvent
import io.plastique.collections.CollectionsEvent.RefreshFinishedEvent
import io.plastique.collections.CollectionsEvent.RetryClickEvent
import io.plastique.collections.CollectionsEvent.SessionChangedEvent
import io.plastique.collections.CollectionsEvent.ShowMatureChangedEvent
import io.plastique.collections.CollectionsEvent.SnackbarShownEvent
import io.plastique.collections.CollectionsEvent.UndoDeleteFolderEvent
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.ContentSettings
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class CollectionsViewModel @Inject constructor(
    stateReducer: CollectionsStateReducer,
    effectHandlerFactory: CollectionsEffectHandlerFactory,
    private val sessionManager: SessionManager,
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    lateinit var state: Observable<CollectionsViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String?) {
        if (::state.isInitialized) return

        val params = FolderLoadParams(username = username, matureContent = contentSettings.showMature)
        val signInNeeded = username == null && sessionManager.session !is Session.User
        val stateAndEffects = if (signInNeeded) {
            next(CollectionsViewState(
                params = params,
                contentState = ContentState.Empty(EmptyState.MessageWithButton(
                    messageResId = R.string.collections_message_sign_in,
                    buttonTextId = R.string.common_button_sign_in)),
                signInNeeded = signInNeeded))
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
            sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) },
            contentSettings.showMatureChanges
                .valveLatest(screenVisible)
                .map { showMature -> ShowMatureChangedEvent(showMature) })
    }

    companion object {
        private const val LOG_TAG = "CollectionsViewModel"
    }
}

@AutoFactory
class CollectionsEffectHandler(
    @Provided private val dataSource: FoldersWithDeviationsDataSource,
    @Provided private val collectionsModel: CollectionsModel,
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
            .switchMapSingle {
                dataSource.loadMore()
                    .toSingleDefault<CollectionsEvent>(LoadMoreFinishedEvent)
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

        return Observable.mergeArray(loadEvents, loadMoreEvents, refreshEvents, createFolderEvents, deleteFolderEvents, undoDeleteFolderEvents)
    }
}

class CollectionsStateReducer @Inject constructor(
    private val connectivityChecker: NetworkConnectivityChecker,
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<CollectionsEvent, CollectionsViewState, CollectionsEffect> {

    override fun reduce(state: CollectionsViewState, event: CollectionsEvent): StateWithEffects<CollectionsViewState, CollectionsEffect> = when (event) {
        is ItemsChangedEvent -> {
            val contentState = if (event.items.isNotEmpty()) {
                ContentState.Content
            } else {
                val emptyState = if (state.params.username != null) {
                    EmptyState.Message(R.string.collections_message_empty_user_collection, listOf(state.params.username.htmlEncode()))
                } else {
                    EmptyState.Message(R.string.collections_message_empty_collection)
                }
                ContentState.Empty(emptyState)
            }
            next(state.copy(
                contentState = contentState,
                items = if (state.isLoadingMore) event.items + LoadingIndicatorItem else event.items,
                collectionItems = event.items,
                hasMore = event.hasMore))
        }

        is LoadErrorEvent -> {
            next(state.copy(
                contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error)),
                items = emptyList(),
                collectionItems = emptyList(),
                hasMore = false))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityChecker.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = state.collectionItems + LoadingIndicatorItem), LoadMoreEffect)
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
                items = state.collectionItems,
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

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCollectionsEffect(state.params))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }

        is SessionChangedEvent -> {
            val signInNeeded = state.params.username == null && event.session !is Session.User
            if (signInNeeded != state.signInNeeded) {
                if (signInNeeded) {
                    next(state.copy(
                        contentState = ContentState.Empty(EmptyState.MessageWithButton(
                            messageResId = R.string.collections_message_sign_in,
                            buttonTextId = R.string.common_button_sign_in)),
                        signInNeeded = signInNeeded))
                } else {
                    next(state.copy(
                        contentState = ContentState.Loading,
                        signInNeeded = signInNeeded
                    ), LoadCollectionsEffect(state.params))
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
                    items = emptyList(),
                    collectionItems = emptyList()),
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
