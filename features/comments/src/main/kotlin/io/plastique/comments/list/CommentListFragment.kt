package io.plastique.comments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.github.technoir42.kotlin.extensions.plus
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsFragmentComponent
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.R
import io.plastique.comments.list.CommentListEvent.CancelReplyClickEvent
import io.plastique.comments.list.CommentListEvent.LoadMoreEvent
import io.plastique.comments.list.CommentListEvent.PostCommentEvent
import io.plastique.comments.list.CommentListEvent.RefreshEvent
import io.plastique.comments.list.CommentListEvent.ReplyClickEvent
import io.plastique.comments.list.CommentListEvent.RetryClickEvent
import io.plastique.comments.list.CommentListEvent.SnackbarShownEvent
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.actionBar
import io.plastique.core.extensions.args
import io.plastique.core.extensions.smartScrollToPosition
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.glide.RecyclerViewPreloader
import io.plastique.inject.getComponent
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CommentListFragment : MvvmFragment<CommentListViewModel>(CommentListViewModel::class.java), ScrollableToTop {
    @Inject lateinit var navigator: CommentsNavigator

    private lateinit var commentsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var composeView: ComposeCommentView
    private lateinit var adapter: CommentListAdapter
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var onScrollListener: EndlessScrollListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val glide = GlideApp.with(this)
        adapter = CommentListAdapter(
            glide = glide,
            onReplyClick = { commentId -> viewModel.dispatch(ReplyClickEvent(commentId)) },
            onReplyingToClick = { commentId -> scrollToComment(commentId) },
            onUserClick = { user -> navigator.openUserProfile(navigationContext, user) })

        commentsView = view.findViewById(R.id.comments)
        commentsView.adapter = adapter
        commentsView.layoutManager = LinearLayoutManager(requireContext())
        commentsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        commentsView.addOnScrollListener(createPreloader(glide, adapter))

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        commentsView.addOnScrollListener(onScrollListener)

        composeView = view.findViewById(R.id.compose)
        composeView.onPostCommentListener = { text -> viewModel.dispatch(PostCommentEvent(text)) }
        composeView.onSignInClickListener = { navigator.openLogin(navigationContext) }
        composeView.onCancelReplyClickListener = { viewModel.dispatch(CancelReplyClickEvent) }

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(this, refreshLayout)

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val threadId = args.getParcelable<CommentThreadId>(ARG_THREAD_ID)!!
        viewModel.init(threadId)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.items, it.first.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second, it.third) }
            .disposeOnDestroy()
    }

    private fun renderState(state: CommentListViewState, prevState: CommentListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        if (activity is CommentListActivity) {
            actionBar.subtitle = state.title
        }

        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.state = state.contentState.emptyState
        }

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.isPagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing

        composeView.isSignedIn = state.isSignedIn
        composeView.isPostingComment = state.isPostingComment
        composeView.replyUsername = state.replyComment?.author?.name
        composeView.isVisible = state.showCompose

        if (state.commentDraft != prevState?.commentDraft) {
            composeView.draft = state.commentDraft
        }

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun createPreloader(glide: GlideRequests, adapter: CommentListAdapter): RecyclerViewPreloader<*> {
        val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_medium)
        val preloadSize = Size(avatarSize, avatarSize)

        val callback = object : RecyclerViewPreloader.Callback<String> {
            override fun getPreloadItems(position: Int): List<String> {
                return when (val item = adapter.items[position]) {
                    is CommentItem -> listOfNotNull(item.comment.author.avatarUrl)
                    else -> emptyList()
                }
            }

            override fun createRequestBuilder(item: String): RequestBuilder<*> {
                return glide.load(item)
                    .circleCrop()
                    .dontAnimate()
                    .skipMemoryCache(true)
                    .priority(Priority.LOW)
            }

            override fun getPreloadSize(item: String): Size = preloadSize
        }

        return RecyclerViewPreloader(glide, lifecycle, callback, maxPreload = 5)
    }

    override fun scrollToTop() {
        if (adapter.itemCount > 0) {
            commentsView.smartScrollToPosition(0)
        }
    }

    private fun scrollToComment(commentId: String) {
        val scrollPosition = adapter.findCommentPosition(commentId)
        commentsView.smartScrollToPosition(scrollPosition)
    }

    override fun injectDependencies() {
        getComponent<CommentsFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_THREAD_ID = "thread_id"
        private const val LOAD_MORE_THRESHOLD = 5

        fun newArgs(threadId: CommentThreadId): Bundle {
            return Bundle().apply {
                putParcelable(ARG_THREAD_ID, threadId)
            }
        }
    }
}
