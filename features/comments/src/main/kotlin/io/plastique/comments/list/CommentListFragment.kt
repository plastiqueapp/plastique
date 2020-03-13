package io.plastique.comments.list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import io.plastique.core.BaseFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.lists.smartScrollToPosition
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CommentListFragment : BaseFragment(R.layout.fragment_comment_list), ScrollableToTop {
    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter
    @Inject lateinit var navigator: CommentsNavigator

    private val viewModel: CommentListViewModel by viewModel()

    private lateinit var commentsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var composeView: ComposeCommentView
    private lateinit var adapter: CommentListAdapter
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var onScrollListener: EndlessScrollListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageLoader = ImageLoader.from(this)
        adapter = CommentListAdapter(
            imageLoader = imageLoader,
            elapsedTimeFormatter = elapsedTimeFormatter,
            onReplyClick = { commentId -> viewModel.dispatch(ReplyClickEvent(commentId)) },
            onReplyingToClick = { commentId -> scrollToComment(commentId) },
            onUserClick = { user -> navigator.openUserProfile(user) })

        commentsView = view.findViewById(R.id.comments)
        commentsView.adapter = adapter
        commentsView.layoutManager = LinearLayoutManager(requireContext())
        commentsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        commentsView.addOnScrollListener(onScrollListener)

        createPreloader(imageLoader, adapter)
            .subscribeToLifecycle(lifecycle)
            .attach(commentsView)

        composeView = view.findViewById(R.id.compose)
        composeView.onPostCommentListener = { text -> viewModel.dispatch(PostCommentEvent(text)) }
        composeView.onSignInClickListener = { navigator.openSignIn() }
        composeView.onCancelReplyClickListener = { viewModel.dispatch(CancelReplyClickEvent) }

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(this, refreshLayout)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val threadId = requireArguments().getParcelable<CommentThreadId>(ARG_THREAD_ID)!!
        viewModel.init(threadId)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
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
        emptyView.state = state.emptyState

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        refreshLayout.isRefreshing = state.listState.isRefreshing

        composeView.isSignedIn = state.isSignedIn
        composeView.isPostingComment = state.isPostingComment
        composeView.replyUsername = state.replyComment?.author?.name
        composeView.isVisible = state.showCompose

        if (state.commentDraft != prevState?.commentDraft) {
            composeView.draft = state.commentDraft
        }

        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    private fun createPreloader(imageLoader: ImageLoader, adapter: CommentListAdapter): ListPreloader {
        val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_medium)
        val callback = ListPreloader.Callback { position, preloader ->
            val item = adapter.items[position] as? CommentItem ?: return@Callback
            val avatarUrl = item.comment.author.avatarUrl
            if (avatarUrl != null) {
                val request = imageLoader.load(avatarUrl)
                    .params {
                        transforms += TransformType.CircleCrop
                        cacheInMemory = false
                    }
                    .createPreloadRequest()
                preloader.preload(request, avatarSize, avatarSize)
            }
        }

        return ListPreloader(imageLoader.glide, callback, MAX_PRELOAD)
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
