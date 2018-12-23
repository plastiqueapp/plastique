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
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsFragmentComponent
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.R
import io.plastique.core.MvvmFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.actionBar
import io.plastique.core.extensions.add
import io.plastique.core.extensions.args
import io.plastique.core.extensions.smartScrollToPosition
import io.plastique.core.extensions.withArguments
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CommentListFragment : MvvmFragment<CommentListViewModel>(), ScrollableToTop {
    private lateinit var commentsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var composeView: ComposeCommentView
    private lateinit var adapter: CommentsAdapter
    private lateinit var contentViewController: ContentViewController
    private lateinit var snackbarController: SnackbarController
    private lateinit var onScrollListener: EndlessScrollListener
    @Inject lateinit var navigator: CommentsNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CommentsAdapter(
                onReplyClick = { commentId -> viewModel.dispatch(CommentListEvent.ReplyClickEvent(commentId)) },
                onReplyingToClick = { commentId -> scrollToComment(commentId) },
                onUserClick = { user -> navigator.openUserProfile(navigationContext, user) })

        commentsView = view.findViewById(R.id.comments)
        commentsView.adapter = adapter
        commentsView.layoutManager = LinearLayoutManager(requireContext())
        commentsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        onScrollListener = EndlessScrollListener(5) { viewModel.dispatch(CommentListEvent.LoadMoreEvent) }
        commentsView.addOnScrollListener(onScrollListener)

        composeView = view.findViewById(R.id.compose)
        composeView.onPostCommentListener = { text -> viewModel.dispatch(CommentListEvent.PostCommentEvent(text)) }
        composeView.onSignInClickListener = { navigator.openLogin(navigationContext) }
        composeView.onCancelReplyClickListener = { viewModel.dispatch(CommentListEvent.CancelReplyClickEvent) }

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(CommentListEvent.RefreshEvent) }

        contentViewController = ContentViewController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(CommentListEvent.RetryClickEvent) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val threadId = args.getParcelable<CommentThreadId>(ARG_THREAD_ID)!!
        viewModel.init(threadId)
        viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second, it.third) }
                .disposeOnDestroy()
    }

    private fun renderState(state: CommentListViewState, prevState: CommentListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        actionBar.subtitle = state.title

        contentViewController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
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
            viewModel.dispatch(CommentListEvent.SnackbarShownEvent)
        }
    }

    override fun scrollToTop() {
        if (adapter.itemCount > 0) {
            commentsView.smartScrollToPosition(0, 10)
        }
    }

    private fun scrollToComment(commentId: String) {
        val scrollPosition = adapter.findCommentPosition(commentId)
        commentsView.smartScrollToPosition(scrollPosition, 10)
    }

    override fun injectDependencies() {
        getComponent<CommentsFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_THREAD_ID = "thread_id"

        fun newInstance(threadId: CommentThreadId): CommentListFragment {
            return CommentListFragment().withArguments {
                putParcelable(ARG_THREAD_ID, threadId)
            }
        }
    }
}
