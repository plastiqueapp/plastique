package io.plastique.comments.list

import com.sch.rxjava2.extensions.ofType
import io.plastique.comments.Comment
import io.plastique.comments.CommentDataSource
import io.plastique.comments.CommentSender
import io.plastique.comments.CommentTarget
import io.plastique.comments.R
import io.plastique.comments.list.CommentListEffect.LoadCommentsEffect
import io.plastique.comments.list.CommentListEffect.LoadMoreEffect
import io.plastique.comments.list.CommentListEffect.LoadTitleEffect
import io.plastique.comments.list.CommentListEffect.PostCommentEffect
import io.plastique.comments.list.CommentListEffect.RefreshEffect
import io.plastique.comments.list.CommentListEvent.CancelReplyClickEvent
import io.plastique.comments.list.CommentListEvent.CommentPostedEvent
import io.plastique.comments.list.CommentListEvent.CommentsChangedEvent
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
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItem
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.deviations.DeviationRepository
import io.plastique.inject.scopes.ActivityScope
import io.plastique.util.NetworkConnectivityMonitor
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class CommentListViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val commentMapper: CommentMapper,
    private val commentDataSource: CommentDataSource,
    private val commentSender: CommentSender,
    private val deviationRepository: DeviationRepository,
    private val errorMessageProvider: ErrorMessageProvider,
    private val sessionManager: SessionManager
) : ViewModel() {

    lateinit var state: Observable<CommentListViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    fun init(target: CommentTarget) {
        if (::state.isInitialized) return

        val initialState = CommentListViewState(
                target = target,
                contentState = ContentState.Loading,
                signedIn = sessionManager.session is Session.User)

        state = loop.loop(initialState, LoadTitleEffect(target), LoadCommentsEffect(target)).disposeOnDestroy()
    }

    fun dispatch(event: CommentListEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<CommentListEffect>): Observable<CommentListEvent> {
        val loadCommentsEvents = effects.ofType<LoadCommentsEffect>()
                .switchMap { effect ->
                    commentDataSource.getData(effect.target)
                            .bindToLifecycle()
                            .map<CommentListEvent> { data ->
                                CommentsChangedEvent(comments = mapComments(data.value), hasMore = data.hasMore)
                            }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(errorMessageProvider.getErrorState(error, R.string.comments_message_load_error)) }
                }

        val loadMoreEvents = effects.ofType<LoadMoreEffect>()
                .flatMapSingle {
                    commentDataSource.loadMore()
                            .toSingleDefault<CommentListEvent>(LoadMoreFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadMoreErrorEvent(errorMessageProvider.getErrorMessage(error, R.string.comments_message_load_error)) }
                }

        val refreshEvents = effects.ofType<RefreshEffect>()
                .flatMapSingle {
                    commentDataSource.refresh()
                            .toSingleDefault<CommentListEvent>(RefreshFinishedEvent)
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> RefreshErrorEvent(errorMessageProvider.getErrorMessage(error, R.string.comments_message_load_error)) }
                }

        val loadTitleEvents = effects.ofType<LoadTitleEffect>()
                .flatMapMaybe { effect ->
                    loadTitle(effect.target)
                            .toMaybe()
                            .doOnError(Timber::e)
                            .onErrorComplete()
                            .map { title -> TitleLoadedEvent(title) }
                }

        val postCommentEvents = effects.ofType<PostCommentEffect>()
                .flatMapSingle { effect ->
                    commentSender.sendComment(effect.target, effect.text, effect.parentCommentId)
                            .map<CommentListEvent> { CommentPostedEvent }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> PostCommentErrorEvent(errorMessageProvider.getErrorMessage(error, R.string.comments_message_post_error)) }
                }

        return Observable.merge(listOf(loadTitleEvents, loadCommentsEvents, loadMoreEvents, refreshEvents, postCommentEvents))
    }

    private fun loadTitle(target: CommentTarget): Single<String> = when (target) {
        is CommentTarget.Deviation -> deviationRepository.getDeviationTitleById(target.deviationId)
        is CommentTarget.Profile -> Single.just(target.username)
        is CommentTarget.Status -> TODO("Not implemented yet")
    }

    private fun externalEvents(): Observable<CommentListEvent> {
        return sessionManager.sessionChanges
                .bindToLifecycle()
                .map { session -> SessionChangedEvent(session) }
    }

    private fun mapComments(comments: List<Comment>): List<CommentUiModel> {
        val commentsById = comments.associateBy { comment -> comment.id }
        return comments.map { comment ->
            val parent = comment.parentId?.let { commentsById[it] }
            commentMapper.map(comment, parent)
        }
    }

    companion object {
        private const val LOG_TAG = "CommentListViewModel"
    }
}

class StateReducer @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
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
            next(state.copy(
                    contentState = ContentState.Empty(event.emptyState, isError = true),
                    items = emptyList(),
                    commentItems = emptyList()))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCommentsEffect(state.target))
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
            next(state.copy(loadingMore = false, items = state.commentItems, snackbarMessage = event.errorMessage))
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

        is PostCommentEvent -> {
            next(state.copy(postingComment = true, commentDraft = event.text),
                    PostCommentEffect(target = state.target, text = event.text, parentCommentId = state.replyComment?.id))
        }

        CommentPostedEvent -> {
            next(state.copy(postingComment = false, commentDraft = ""))
        }

        is PostCommentErrorEvent -> {
            next(state.copy(postingComment = false, snackbarMessage = event.errorMessage))
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
            next(state.copy(snackbarMessage = null))
        }
    }

    private fun createItems(comments: List<CommentUiModel>, showReplyButton: Boolean): List<ListItem> {
        return comments.map { CommentItem(it, showReplyButton) }
    }

    private fun mergeItems(commentItems: List<ListItem>, loadingMore: Boolean): List<ListItem> {
        return if (loadingMore) commentItems + LoadingIndicatorItem else commentItems
    }
}
