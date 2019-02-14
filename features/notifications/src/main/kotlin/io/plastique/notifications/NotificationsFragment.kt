package io.plastique.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.MvvmFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.add
import io.plastique.core.extensions.smartScrollToPosition
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.plastique.notifications.NotificationsEvent.LoadMoreEvent
import io.plastique.notifications.NotificationsEvent.RefreshEvent
import io.plastique.notifications.NotificationsEvent.RetryClickEvent
import io.plastique.notifications.NotificationsEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class NotificationsFragment : MvvmFragment<NotificationsViewModel>(), MainPage, ScrollableToTop {
    private lateinit var notificationsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var adapter: NotificationsAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var state: NotificationsViewState
    @Inject lateinit var navigator: NotificationsNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
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
        emptyView.setOnButtonClickListener {
            if (!state.isSignedIn) {
                navigator.openLogin(navigationContext)
            } else {
                viewModel.dispatch(RetryClickEvent)
            }
        }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)

        onScrollListener = EndlessScrollListener(4, isEnabled = false) { viewModel.dispatch(LoadMoreEvent) }
        notificationsView.addOnScrollListener(onScrollListener)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.init()
        viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second, it.third) }
                .disposeOnDestroy()
    }

    private fun renderState(state: NotificationsViewState, prevState: NotificationsViewState?, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state

        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
        }

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.pagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun createAdapter(): NotificationsAdapter {
        return NotificationsAdapter(
                glide = GlideApp.with(this),
                onOpenCollection = { username, folderId, folderName -> navigator.openCollectionFolder(navigationContext, username, folderId, folderName) },
                onOpenComment = { },
                onOpenDeviation = { navigator.openDeviation(navigationContext, it) },
                onOpenStatus = { navigator.openStatus(navigationContext, it) },
                onOpenUserProfile = { navigator.openUserProfile(navigationContext, it) })
    }

    override fun getTitle(): Int = R.string.notifications_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun scrollToTop() {
        notificationsView.smartScrollToPosition(0, 10)
    }

    override fun injectDependencies() {
        getComponent<NotificationsFragmentComponent>().inject(this)
    }
}
