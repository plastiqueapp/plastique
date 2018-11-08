package io.plastique.comments.list

import com.sch.rxjava2.extensions.ofType
import io.plastique.comments.Comment
import io.plastique.comments.CommentDataSource
import io.plastique.comments.CommentSender
import io.plastique.comments.CommentThreadId
import io.plastique.comments.R
import io.plastique.comments.list.CommentListEffect.LoadCommentsEffect
import io.plastique.comments.list.CommentListEffect.LoadMoreEffect
import io.plastique.comments.list.CommentListEffect.LoadTitleEffect
import io.plastique.comments.list.CommentListEffect.PostCommentEffect
import io.plastique.comments.list.CommentListEffect.RefreshEffect
import io.plastique.comments.list.CommentListEvent.CancelReplyClickEvent
import io.plastique.comments.list.CommentListEvent.CommentPostedEvent
import io.plastique.comments.list.CommentListEvent.CommentsChangedEvent
import io.plastique.comments.list.CommentListEvent.ConnectionStateChangedEvent
import io.plastique.comments.list.CommentListEvent.LoadErrorEvent
import io.plastique.comments.list.CommentListEvent.LoadMoreErrorEvent
import io.plastique.comments.list.CommentListEvent.LoadMoreEvent
import io.plastique.comments.list.CommentListEvent.LoadMoreFinishedEvent
import io.plastique.comments.list.CommentListEvent.PostCommentErrorEvent
import io.plastique.comments.list.CommentListEvent.PostCommentEvent
import io.plastique.comments.list.CommentListEvent.RefreshErrorEvent
import io.plastique.comments.list.CommentListEvent.RefreshEvent
import io.plastique.comments.list.CommentListEvent.RefreshFinishedEvent
import io.plastique.comments.list.CommentListEvent.ReplyClickEvent
import io.plastique.comments.list.CommentListEvent.RetryClickEvent
import io.plastique.comments.list.CommentListEvent.SessionChangedEvent
import io.plastique.comments.list.CommentListEvent.SnackbarShownEvent
import io.plastique.comments.list.CommentListEvent.TitleLoadedEvent
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.exceptions.NoNetworkConnectionException
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.DeviationRepository
import io.plastique.inject.scopes.ActivityScope
import io.plastique.util.NetworkConnectionState
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class CommentListViewModel @Inject constructor(
    stateReducer: CommentListStateReducer,
    private val commentDataSource: CommentDataSource,
    private val commentSender: CommentSender,
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val deviationRepository: DeviationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    lateinit var state: Observable<CommentListViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(threadId: CommentThreadId) {
        if (::state.isInitialized) return

        val initialState = CommentListViewState(
                threadId = threadId,
                contentState = ContentState.Loading,
                signedIn = sessionManager.session is Session.User)

        state = loop.loop(initialState, LoadTitleEffect(threadId), LoadCommentsEffect(threadId)).disposeOnDestroy()
    }

    fun dispatch(event: CommentListEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<CommentListEffect>): Observable<CommentListEvent> {
        val loadCommentsEvents = effects.ofType<LoadCommentsEffect>()
                .switchMap { effect ->
                    commentDataSource.getData(effect.threadId)
                            .bindToLifecycle()
                            .map<CommentListEvent> { data ->
                                CommentsChangedEvent(comments = mapComments(data.value), hasMore = data.hasMore)
                            }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .flatMapSingle {
                    commentDataSource.loadMore()
                            .toSingleDefault<CommentListEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(error) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .flatMapSingle {
                    commentDataSource.refresh()
                            .toSingleDefault<CommentListEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(error) }
                }

        val loadTitleEvents = effects.ofType<LoadTitleEffect>()
                .flatMapMaybe { effect ->
                    loadTitle(effect.threadId)
                            .toMaybe()
                            .doOnError(Timber::e)
                            .onErrorComplete()
                            .map { title -> TitleLoadedEvent(title) }
                }

        val postCommentEvents = effects.ofType<PostCommentEffect>()
                .flatMapSingle { effect ->
                    commentSender.sendComment(effect.threadId, effect.text, effect.parentCommentId)
                            .map<CommentListEvent> { CommentPostedEvent }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> PostCommentErrorEvent(error) }
                }

        return Observable.merge(listOf(loadTitleEvents, loadCommentsEvents, loadMoreEvents, refreshEvents, postCommentEvents))
    }

    private fun loadTitle(threadId: CommentThreadId): Single<String> = when (threadId) {
        is CommentThreadId.Deviation -> deviationRepository.getDeviationTitleById(threadId.deviationId)
        is CommentThreadId.Profile -> Single.just(threadId.username)
        is CommentThreadId.Status -> TODO("Not implemented yet")
    }

    private fun externalEvents(): Observable<CommentListEvent> {
        return Observable.merge(
                connectivityMonitor.connectionState
                        .bindToLifecycle()
                        .map { connectionState -> ConnectionStateChangedEvent(connectionState) },
                sessionManager.sessionChanges
                        .bindToLifecycle()
                        .map { session -> SessionChangedEvent(session) })
    }

    private fun mapComments(comments: List<Comment>): List<CommentUiModel> {
        val commentsById = comments.associateBy { comment -> comment.id }
        return comments.map { comment ->
            val parent = comment.parentId?.let { commentsById[it] }
            comment.toCommentUiModel(parent)
        }
    }

    companion object {
        private const val LOG_TAG = "CommentListViewModel"
    }
}

class CommentListStateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : Reducer<CommentListEvent, CommentListViewState, CommentListEffect> {
    override fun invoke(state: CommentListViewState, event: CommentListEvent): Next<CommentListViewState, CommentListEffect> = when (event) {
        is CommentsChangedEvent -> {
            val commentItems = createItems(event.comments, state.signedIn)
            val contentState = if (commentItems.isEmpty()) {
                ContentState.Empty(EmptyState(message = resourceProvider.getString(R.string.comments_message_empty)))
            } else {
                ContentState.Content
            }
            next(state.copy(
                    contentState = contentState,
                    comments = event.comments,
                    hasMore = event.hasMore,
                    commentItems = commentItems,
                    items = mergeItems(commentItems, state.loadingMore)))
        }

        is LoadErrorEvent -> {
            val errorState = errorMessageProvider.getErrorState(event.error, R.string.comments_message_load_error)
            next(state.copy(
                    contentState = ContentState.Empty(errorState, isError = true, error = event.error),
                    items = emptyList(),
                    commentItems = emptyList()))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCommentsEffect(state.threadId))
        }

        LoadMoreEvent -> {
            if (!state.loadingMore && connectivityMonitor.isConnectedToNetwork) {
                next(state.copy(loadingMore = true, items = mergeItems(state.commentItems, true)), LoadMoreEffect)
            } else {
                next(state)
            }
        }

        LoadMoreFinishedEvent -> {
            next(state.copy(loadingMore = false))
        }

        is LoadMoreErrorEvent -> {
            next(state.copy(
                    loadingMore = false,
                    items = state.commentItems,
                    snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.comments_message_load_error))))
        }

        RefreshEvent -> {
            next(state.copy(isRefreshing = true), RefreshEffect)
        }

        RefreshFinishedEvent -> {
            next(state.copy(isRefreshing = false))
        }

        is RefreshErrorEvent -> {
            next(state.copy(isRefreshing = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.comments_message_load_error))))
        }

        is PostCommentEvent -> {
            next(state.copy(postingComment = true, commentDraft = event.text),
                    PostCommentEffect(threadId = state.threadId, text = event.text, parentCommentId = state.replyComment?.id))
        }

        CommentPostedEvent -> {
            next(state.copy(postingComment = false, commentDraft = ""))
        }

        is PostCommentErrorEvent -> {
            next(state.copy(postingComment = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.comments_message_post_error))))
        }

        is ReplyClickEvent -> {
            val replyComment = state.comments.find { comment -> comment.id == event.commentId }
            next(state.copy(replyComment = replyComment))
        }

        CancelReplyClickEvent -> {
            next(state.copy(replyComment = null))
        }

        is TitleLoadedEvent -> {
            next(state.copy(title = event.title))
        }

        is ConnectionStateChangedEvent -> {
            if (event.connectionState === NetworkConnectionState.Connected &&
                    state.contentState is ContentState.Empty &&
                    state.contentState.error is NoNetworkConnectionException) {
                next(state.copy(
                        contentState = ContentState.Loading),
                        LoadCommentsEffect(state.threadId))
            } else {
                next(state)
            }
        }

        is SessionChangedEvent -> {
            val signedIn = event.session is Session.User
            if (state.signedIn != signedIn) {
                val items = createItems(state.comments, signedIn)
                next(state.copy(
                        signedIn = signedIn,
                        commentItems = items,
                        items = mergeItems(items, state.loadingMore),
                        replyComment = null))
            } else {
                next(state)
            }
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }
    }

    private fun createItems(comments: List<CommentUiModel>, showReplyButton: Boolean): List<ListItem> {
        return comments.map { CommentItem(it, showReplyButton) }
    }

    private fun mergeItems(commentItems: List<ListItem>, loadingMore: Boolean): List<ListItem> {
        return if (loadingMore) commentItems + LoadingIndicatorItem else commentItems
    }
}
