package io.plastique.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseFragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.image.ImageLoader
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.calculateDiff
import io.plastique.core.lists.smartScrollToPosition
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.plastique.notifications.NotificationsEvent.DeleteMessageEvent
import io.plastique.notifications.NotificationsEvent.LoadMoreEvent
import io.plastique.notifications.NotificationsEvent.RefreshEvent
import io.plastique.notifications.NotificationsEvent.RetryClickEvent
import io.plastique.notifications.NotificationsEvent.SnackbarShownEvent
import io.plastique.notifications.NotificationsEvent.UndoDeleteMessageEvent
import io.plastique.notifications.databinding.FragmentNotificationsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class NotificationsFragment : BaseFragment(), MainPage, ScrollableToTop {
    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter

    private val viewModel: NotificationsViewModel by viewModel()
    private val navigator: NotificationsNavigator get() = viewModel.navigator

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        notificationsAdapter = createAdapter()
        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }

        binding.notifications.apply {
            adapter = notificationsAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }
        binding.refresh.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, binding.refresh, binding.progress, binding.empty)
        snackbarController = SnackbarController(this, binding.refresh)
        snackbarController.onActionClick = { actionData -> viewModel.dispatch(UndoDeleteMessageEvent(actionData as String)) }
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

        initSwipe()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.init()
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
            .disposeOnDestroy()
    }

    private fun renderState(state: NotificationsViewState, listUpdateData: ListUpdateData<ListItem>) {
        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState
        binding.refresh.isRefreshing = state.listState.isRefreshing
        onScrollListener.isEnabled = state.listState.isPagingEnabled
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(notificationsAdapter)
    }

    private fun createAdapter(): NotificationsAdapter {
        return NotificationsAdapter(
            imageLoader = ImageLoader.from(this),
            elapsedTimeFormatter = elapsedTimeFormatter,
            onCollectionFolderClick = { folderId, folderName -> navigator.openCollectionFolder(folderId, folderName) },
            onCommentClick = { /* TODO */ },
            onDeviationClick = { navigator.openDeviation(it) },
            onStatusClick = { navigator.openStatus(it) },
            onUserClick = { navigator.openUserProfile(it) })
    }

    private fun initSwipe() {
        val swipeCallback = object : ItemTouchHelper.Callback() {
            override fun onMove(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val item = notificationsAdapter.items[holder.adapterPosition] as NotificationItem
                viewModel.dispatch(DeleteMessageEvent(item.messageId))
            }

            override fun getMovementFlags(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder): Int {
                return if (holder.itemViewType != LoadingIndicatorItemDelegate.VIEW_TYPE) {
                    makeMovementFlags(0, ItemTouchHelper.RIGHT)
                } else {
                    0
                }
            }

            override fun isLongPressDragEnabled(): Boolean = false
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.notifications)
    }

    override fun getTitle(): Int = R.string.notifications_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun scrollToTop() {
        binding.notifications.smartScrollToPosition(0)
    }

    override fun injectDependencies() {
        getComponent<NotificationsFragmentComponent>().inject(this)
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 4
    }
}
