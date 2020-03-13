package io.plastique.notifications

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseFragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
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
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications), MainPage, ScrollableToTop {
    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter

    private val viewModel: NotificationsViewModel by viewModel()
    private val navigator: NotificationsNavigator get() = viewModel.navigator

    private lateinit var notificationsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var adapter: NotificationsAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = createAdapter()

        notificationsView = view.findViewById(R.id.notifications)
        notificationsView.adapter = adapter
        notificationsView.layoutManager = LinearLayoutManager(context)
        notificationsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(this, refreshLayout)
        snackbarController.onActionClickListener = { actionData -> viewModel.dispatch(UndoDeleteMessageEvent(actionData as String)) }
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        notificationsView.addOnScrollListener(onScrollListener)

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
        emptyView.state = state.emptyState

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        refreshLayout.isRefreshing = state.listState.isRefreshing
        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    private fun createAdapter(): NotificationsAdapter {
        return NotificationsAdapter(
            imageLoader = ImageLoader.from(this),
            elapsedTimeFormatter = elapsedTimeFormatter,
            onOpenCollection = { username, folderId, folderName -> navigator.openCollectionFolder(username, folderId, folderName) },
            onOpenComment = { /* TODO */ },
            onOpenDeviation = { navigator.openDeviation(it) },
            onOpenStatus = { navigator.openStatus(it) },
            onOpenUserProfile = { navigator.openUserProfile(it) })
    }

    private fun initSwipe() {
        val swipeCallback = object : ItemTouchHelper.Callback() {
            override fun onMove(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.items[holder.adapterPosition] as NotificationItem
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

        ItemTouchHelper(swipeCallback).attachToRecyclerView(notificationsView)
    }

    override fun getTitle(): Int = R.string.notifications_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun scrollToTop() {
        notificationsView.smartScrollToPosition(0)
    }

    override fun injectDependencies() {
        getComponent<NotificationsFragmentComponent>().inject(this)
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 4
    }
}
