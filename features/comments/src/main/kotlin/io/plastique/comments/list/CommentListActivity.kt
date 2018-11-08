package io.plastique.comments.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsActivityComponent
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.R
import io.plastique.comments.list.CommentListEvent.CancelReplyClickEvent
import io.plastique.comments.list.CommentListEvent.LoadMoreEvent
import io.plastique.comments.list.CommentListEvent.PostCommentEvent
import io.plastique.comments.list.CommentListEvent.RefreshEvent
import io.plastique.comments.list.CommentListEvent.ReplyClickEvent
import io.plastique.comments.list.CommentListEvent.RetryClickEvent
import io.plastique.comments.list.CommentListEvent.SnackbarShownEvent
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.setSubtitleOnClickListener
import io.plastique.core.extensions.setTitleOnClickListener
import io.plastique.core.extensions.smartScrollToPosition
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListItemDiffTransformer
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CommentListActivity : MvvmActivity<CommentListViewModel>() {
    private lateinit var commentsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var composeView: ComposeCommentView
    private lateinit var adapter: CommentsAdapter
    private lateinit var contentViewController: ContentViewController
    private lateinit var snackbarController: SnackbarController
    private lateinit var onScrollListener: EndlessScrollListener
    @Inject lateinit var navigator: CommentsNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_list)
        initToolbar()

        adapter = CommentsAdapter()
        adapter.onOpenUserProfileListener = { navigator.openUserProfile(navigationContext, it.name) }
        adapter.onReplyClickListener = { commentId -> viewModel.dispatch(ReplyClickEvent(commentId)) }
        adapter.onReplyingToClickListener = { commentId -> scrollToComment(commentId) }

        commentsView = findViewById(R.id.comments)
        commentsView.adapter = adapter
        commentsView.layoutManager = LinearLayoutManager(this)
        commentsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        onScrollListener = EndlessScrollListener(5) { viewModel.dispatch(LoadMoreEvent) }
        commentsView.addOnScrollListener(onScrollListener)

        composeView = findViewById(R.id.compose)
        composeView.onPostCommentListener = { text -> viewModel.dispatch(PostCommentEvent(text)) }
        composeView.onSignInClickListener = { navigator.openLogin(navigationContext) }
        composeView.onCancelReplyClickListener = { viewModel.dispatch(CancelReplyClickEvent) }

        refreshLayout = findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentViewController = ContentViewController(this, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener { viewModel.dispatch(RetryClickEvent) })

        viewModel.init(intent.getParcelableExtra(EXTRA_THREAD_ID))
        observeState()
    }

    private fun observeState() {
        viewModel.state
                .map { state -> state.title }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { title -> supportActionBar!!.subtitle = title }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.contentState }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentState ->
                    contentViewController.state = contentState
                    if (contentState is ContentState.Empty) {
                        emptyView.setState(contentState.emptyState)
                    }
                }
                .disposeOnDestroy()

        @Suppress("RemoveExplicitTypeArguments")
        viewModel.state
                .map { state -> state.items }
                .compose(ListItemDiffTransformer<ListItem>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateData -> updateData.applyTo(adapter) }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.isPagingEnabled }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> onScrollListener.isEnabled = state.isPagingEnabled }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.isRefreshing }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> refreshLayout.isRefreshing = state.isRefreshing }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.signedIn }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { signedIn ->
                    if (signedIn) {
                        composeView.showCompose()
                    } else {
                        composeView.showSignIn()
                    }
                }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.showCompose }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showCompose -> composeView.isVisible = showCompose }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.postingComment }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { postingComment -> composeView.setPosting(postingComment) }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.replyComment }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> composeView.setReplyUserName(state.replyComment?.author?.name) }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.commentDraft }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { draft -> composeView.setDraft(draft) }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.snackbarState }
                .filter { state -> state.snackbarState !== SnackbarState.None }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    snackbarController.showSnackbar(state.snackbarState)
                    viewModel.dispatch(SnackbarShownEvent)
                }
                .disposeOnDestroy()
    }

    private fun scrollToComment(commentId: String) {
        val scrollPosition = adapter.findCommentPosition(commentId)
        commentsView.smartScrollToPosition(scrollPosition, 10)
    }

    override fun injectDependencies() {
        getComponent<CommentsActivityComponent>().inject(this)
    }

    private fun initToolbar() {
        val toolbar = setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        val onClickListener = View.OnClickListener {
            if (adapter.itemCount > 0) {
                commentsView.smartScrollToPosition(0, 10)
            }
        }
        toolbar.setTitleOnClickListener(onClickListener)
        toolbar.setSubtitleOnClickListener(onClickListener)
    }

    companion object {
        private const val EXTRA_THREAD_ID = "thread_id"

        fun createIntent(context: Context, threadId: CommentThreadId): Intent {
            return Intent(context, CommentListActivity::class.java).apply {
                putExtra(EXTRA_THREAD_ID, threadId)
            }
        }
    }
}
