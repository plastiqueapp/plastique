package io.plastique.comments.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.glide.preloader.ListPreloader
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsFragmentComponent
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.R
import io.plastique.comments.databinding.FragmentCommentListBinding
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

class CommentListFragment : BaseFragment(), ScrollableToTop {
    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter
    @Inject lateinit var navigator: CommentsNavigator

    private val viewModel: CommentListViewModel by viewModel()

    private lateinit var binding: FragmentCommentListBinding
    private lateinit var commentListAdapter: CommentListAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageLoader = ImageLoader.from(this)
        commentListAdapter = CommentListAdapter(
            imageLoader = imageLoader,
            elapsedTimeFormatter = elapsedTimeFormatter,
            onReplyClick = { commentId -> viewModel.dispatch(ReplyClickEvent(commentId)) },
            onReplyingToClick = { commentId -> scrollToComment(commentId) },
            onUserClick = { user -> navigator.openUserProfile(user) })

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }

        binding.comments.apply {
            adapter = commentListAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }

        createPreloader(imageLoader, commentListAdapter)
            .subscribeToLifecycle(lifecycle)
            .attach(binding.comments)

        binding.compose.apply {
            onPostCommentClick = { text -> viewModel.dispatch(PostCommentEvent(text)) }
            onSignInClick = { navigator.openSignIn() }
            onCancelReplyClick = { viewModel.dispatch(CancelReplyClickEvent) }
        }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }
        binding.refresh.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, binding.refresh, binding.progress, binding.empty)
        snackbarController = SnackbarController(this, binding.refresh)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }
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
        binding.empty.state = state.emptyState
        binding.refresh.isRefreshing = state.listState.isRefreshing
        onScrollListener.isEnabled = state.listState.isPagingEnabled
        binding.compose.apply {
            isSignedIn = state.isSignedIn
            isPostingComment = state.isPostingComment
            replyUsername = state.replyComment?.author?.name
            isVisible = state.showCompose
            if (state.commentDraft != prevState?.commentDraft) {
                draft = state.commentDraft
            }
        }
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(commentListAdapter)
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
        if (commentListAdapter.itemCount > 0) {
            binding.comments.smartScrollToPosition(0)
        }
    }

    private fun scrollToComment(commentId: String) {
        val scrollPosition = commentListAdapter.findCommentPosition(commentId)
        binding.comments.smartScrollToPosition(scrollPosition)
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
