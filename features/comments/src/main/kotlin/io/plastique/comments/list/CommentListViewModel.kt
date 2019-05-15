package io.plastique.comments.list

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import com.sch.rxjava2.extensions.valveLatest
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
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.BaseViewModel
import io.plastique.core.ResourceProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.network.NetworkConnectionState
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NetworkConnectivityMonitor
import io.plastique.core.network.NoNetworkConnectionException
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.snackbar.SnackbarState
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.DeviationRepository
import io.plastique.inject.scopes.FragmentScope
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class CommentListViewModel @Inject constructor(
    stateReducer: CommentListStateReducer,
    effectHandlerFactory: CommentListEffectHandlerFactory,
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
            sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) })
    }

    companion object {
        private const val LOG_TAG = "CommentListViewModel"
    }
}

@AutoFactory
class CommentListEffectHandler(
    @Provided private val commentDataSource: CommentDataSource,
    @Provided private val commentSender: CommentSender,
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
        if (parentId != parent?.id) {
            throw IllegalArgumentException("Expected parent comment with id $parentId but got ${parent?.id}")
        }
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
    private val connectivityChecker: NetworkConnectivityChecker,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : StateReducer<CommentListEvent, CommentListViewState, CommentListEffect> {

    override fun reduce(state: CommentListViewState, event: CommentListEvent): StateWithEffects<CommentListViewState, CommentListEffect> = when (event) {
        is CommentsChangedEvent -> {
            val commentItems = createItems(event.comments, state.isSignedIn)
            val contentState = if (commentItems.isEmpty()) {
                ContentState.Empty(EmptyState.Message(resourceProvider.getString(R.string.comments_message_empty)))
            } else {
                ContentState.Content
            }
            next(state.copy(
                contentState = contentState,
                comments = event.comments,
                hasMore = event.hasMore,
                commentItems = commentItems,
                items = mergeItems(commentItems, state.isLoadingMore)))
        }

        is LoadErrorEvent -> {
            val errorState = errorMessageProvider.getErrorState(event.error, R.string.comments_message_load_error)
            next(state.copy(
                contentState = ContentState.Empty(isError = true, error = event.error, emptyState = errorState),
                items = emptyList(),
                commentItems = emptyList()))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCommentsEffect(state.threadId))
        }

        LoadMoreEvent -> {
            if (!state.isLoadingMore && connectivityChecker.isConnectedToNetwork) {
                next(state.copy(isLoadingMore = true, items = mergeItems(state.commentItems, true)), LoadMoreEffect)
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
            next(state.copy(
                isRefreshing = false,
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.comments_message_load_error))))
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
                snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessage(event.error, R.string.comments_message_post_error))))
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
            val isSignedIn = event.session is Session.User
            if (state.isSignedIn != isSignedIn) {
                val items = createItems(state.comments, isSignedIn)
                next(state.copy(
                    isSignedIn = isSignedIn,
                    commentItems = items,
                    items = mergeItems(items, state.isLoadingMore),
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

    private fun mergeItems(commentItems: List<ListItem>, isLoadingMore: Boolean): List<ListItem> {
        return if (isLoadingMore) commentItems + LoadingIndicatorItem else commentItems
    }
}
