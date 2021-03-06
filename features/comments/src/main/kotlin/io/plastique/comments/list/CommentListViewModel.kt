package io.plastique.comments.list

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
import io.plastique.comments.Comment
import io.plastique.comments.CommentDataSource
import io.plastique.comments.CommentSender
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsNavigator
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
import io.plastique.comments.list.CommentListEvent.LoadMoreStartedEvent
import io.plastique.comments.list.CommentListEvent.PostCommentErrorEvent
import io.plastique.comments.list.CommentListEvent.PostCommentEvent
import io.plastique.comments.list.CommentListEvent.RefreshErrorEvent
import io.plastique.comments.list.CommentListEvent.RefreshEvent
import io.plastique.comments.list.CommentListEvent.RefreshFinishedEvent
import io.plastique.comments.list.CommentListEvent.ReplyClickEvent
import io.plastique.comments.list.CommentListEvent.RetryClickEvent
import io.plastique.comments.list.CommentListEvent.SnackbarShownEvent
import io.plastique.comments.list.CommentListEvent.TitleLoadedEvent
import io.plastique.comments.list.CommentListEvent.UserChangedEvent
import io.plastique.common.ErrorMessageProvider
import io.plastique.common.ErrorType
import io.plastique.common.toErrorType
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.lists.PagedListState
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.network.NetworkConnectionState
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NetworkConnectivityMonitor
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
import io.plastique.core.snackbar.SnackbarState
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.DeviationRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class CommentListViewModel @Inject constructor(
    stateReducer: CommentListStateReducer,
    effectHandlerFactory: CommentListEffectHandlerFactory,
    val navigator: CommentsNavigator,
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    lateinit var state: Observable<CommentListViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(threadId: CommentThreadId) {
        if (::state.isInitialized) return

        val initialState = CommentListViewState(
            threadId = threadId,
            contentState = ContentState.Loading,
            isSignedIn = sessionManager.session is Session.User)

        state = loop.loop(initialState, LoadTitleEffect(threadId), LoadCommentsEffect(threadId)).disposeOnDestroy()
    }

    fun dispatch(event: CommentListEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<CommentListEvent> {
        return Observable.merge(
            connectivityMonitor.connectionState
                .valveLatest(screenVisible)
                .map { connectionState -> ConnectionStateChangedEvent(connectionState) },
            sessionManager.userIdChanges
                .skip(1)
                .valveLatest(screenVisible)
                .map { userId -> UserChangedEvent(userId.toNullable()) })
    }

    companion object {
        private const val LOG_TAG = "CommentListViewModel"
    }
}

@AutoFactory
class CommentListEffectHandler(
    @Provided private val commentDataSource: CommentDataSource,
    @Provided private val commentSender: CommentSender,
    @Provided private val connectivityChecker: NetworkConnectivityChecker,
    @Provided private val deviationRepository: DeviationRepository,
    @Provided private val richTextFormatter: RichTextFormatter,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<CommentListEffect, CommentListEvent> {

    override fun handle(effects: Observable<CommentListEffect>): Observable<CommentListEvent> {
        val loadCommentsEvents = effects.ofType<LoadCommentsEffect>()
            .switchMap { effect ->
                commentDataSource.getData(effect.threadId)
                    .valveLatest(screenVisible)
                    .map<CommentListEvent> { data ->
                        CommentsChangedEvent(comments = mapComments(data.value), hasMore = data.hasMore)
                    }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
            .filter { connectivityChecker.isConnectedToNetwork }
            .switchMap {
                commentDataSource.loadMore()
                    .toObservable<CommentListEvent>()
                    .surroundWith(LoadMoreStartedEvent, LoadMoreFinishedEvent)
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
                    .toSingleDefault<CommentListEvent>(CommentPostedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> PostCommentErrorEvent(error) }
            }

        return Observable.merge(listOf(loadTitleEvents, loadCommentsEvents, loadMoreEvents, refreshEvents, postCommentEvents))
    }

    private fun loadTitle(threadId: CommentThreadId): Single<String> = when (threadId) {
        is CommentThreadId.Deviation -> deviationRepository.getDeviationTitleById(threadId.deviationId)
        is CommentThreadId.Profile -> Single.just(threadId.username)
        is CommentThreadId.Status -> Single.just("")
    }

    private fun mapComments(comments: List<Comment>): List<CommentUiModel> {
        val commentsById = comments.associateBy { comment -> comment.id }
        return comments.map { comment ->
            val parent = comment.parentId?.let { commentsById[it] }
            comment.toCommentUiModel(parent)
        }
    }

    private fun Comment.toCommentUiModel(parent: Comment?): CommentUiModel {
        require(parentId == parent?.id) { "Expected parent comment with id $parentId but got ${parent?.id}" }
        return CommentUiModel(
            id = id,
            datePosted = datePosted,
            text = SpannedWrapper(richTextFormatter.format(text)),
            author = author,
            parentId = parentId,
            parentAuthorName = parent?.author?.name)
    }
}

class CommentListStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<CommentListEvent, CommentListViewState, CommentListEffect> {

    override fun reduce(state: CommentListViewState, event: CommentListEvent): StateWithEffects<CommentListViewState, CommentListEffect> = when (event) {
        is CommentsChangedEvent -> {
            if (event.comments.isNotEmpty()) {
                val commentItems = createItems(event.comments, state.isSignedIn)
                next(state.copy(
                    contentState = ContentState.Content,
                    errorType = ErrorType.None,
                    comments = event.comments,
                    listState = state.listState.copy(
                        items = if (state.listState.isLoadingMore) commentItems + LoadingIndicatorItem else commentItems,
                        contentItems = commentItems,
                        hasMore = event.hasMore),
                    emptyState = null))
            } else {
                next(state.copy(
                    contentState = ContentState.Empty,
                    errorType = ErrorType.None,
                    comments = emptyList(),
                    listState = PagedListState.Empty,
                    emptyState = EmptyState.Message(R.string.comments_message_empty)))
            }
        }

        is LoadErrorEvent -> {
            next(state.copy(
                contentState = ContentState.Empty,
                errorType = event.error.toErrorType(),
                listState = PagedListState.Empty,
                emptyState = errorMessageProvider.getErrorState(event.error, R.string.comments_message_load_error)))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading, errorType = ErrorType.None, emptyState = null), LoadCommentsEffect(state.threadId))
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
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.comments_message_load_error))))
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
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.comments_message_load_error))))
        }

        is PostCommentEvent -> {
            next(state.copy(isPostingComment = true, commentDraft = event.text),
                PostCommentEffect(threadId = state.threadId, text = event.text, parentCommentId = state.replyComment?.id))
        }

        CommentPostedEvent -> {
            next(state.copy(isPostingComment = false, commentDraft = "", replyComment = null))
        }

        is PostCommentErrorEvent -> {
            next(state.copy(
                isPostingComment = false,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error, R.string.comments_message_post_error))))
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
            if (event.connectionState == NetworkConnectionState.Connected &&
                state.contentState == ContentState.Empty &&
                state.errorType == ErrorType.NoNetworkConnection) {
                next(state.copy(contentState = ContentState.Loading, errorType = ErrorType.None, emptyState = null), LoadCommentsEffect(state.threadId))
            } else {
                next(state)
            }
        }

        is UserChangedEvent -> {
            val isSignedIn = event.userId != null
            if (state.isSignedIn != isSignedIn) {
                val items = createItems(state.comments, isSignedIn)
                next(state.copy(
                    isSignedIn = isSignedIn,
                    listState = state.listState.copy(
                        items = if (state.listState.isLoadingMore) items + LoadingIndicatorItem else items,
                        contentItems = items),
                    replyComment = null))
            } else {
                next(state)
            }
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = null))
        }
    }

    private fun createItems(comments: List<CommentUiModel>, showReplyButton: Boolean): List<ListItem> {
        return comments.map { CommentItem(it, showReplyButton) }
    }
}
