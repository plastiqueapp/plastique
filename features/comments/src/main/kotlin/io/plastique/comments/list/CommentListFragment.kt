package io.plastique.comments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Priority
import com.github.technoir42.glide.preloader.ListPreloader
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
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
import io.plastique.core.extensions.smartScrollToPosition
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CommentListFragment : MvvmFragment<CommentListViewModel>(CommentListViewModel::class.java), ScrollableToTop {
    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter
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
            elapsedTimeFormatter = elapsedTimeFormatter,
            onReplyClick = { commentId -> viewModel.dispatch(ReplyClickEvent(commentId)) },
            onReplyingToClick = { commentId -> scrollToComment(commentId) },
            onUserClick = { user -> navigator.openUserProfile(navigationContext, user) })

        commentsView = view.findViewById(R.id.comments)
        commentsView.adapter = adapter
        commentsView.layoutManager = LinearLayoutManager(requireContext())
        commentsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        commentsView.addOnScrollListener(onScrollListener)

        createPreloader(glide, adapter)
            .subscribeToLifecycle(lifecycle)
            .attach(commentsView)

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

        val threadId = requireArguments().getParcelable<CommentThreadId>(ARG_THREAD_ID)!!
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
            val actionBar = (activity as AppCompatActivity).supportActionBar!!
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

        if (state.snackbarState != null && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun createPreloader(glide: GlideRequests, adapter: CommentListAdapter): ListPreloader {
        val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_medium)
        val callback = ListPreloader.Callback { position, preloader ->
            val item = adapter.items[position] as? CommentItem ?: return@Callback
            val avatarUrl = item.comment.author.avatarUrl
            if (avatarUrl != null) {
                val request = glide.load(avatarUrl)
                    .circleCrop()
                    .dontAnimate()
                    .priority(Priority.LOW)
                    .skipMemoryCache(true)

                preloader.preload(request, avatarSize, avatarSize)
            }
        }

        return ListPreloader(glide, callback, MAX_PRELOAD)
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
        private const val MAX_PRELOAD = 5

        fun newArgs(threadId: CommentThreadId): Bundle {
            return Bundle().apply {
                putParcelable(ARG_THREAD_ID, threadId)
            }
        }
    }
}
