package io.plastique.watch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.add
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.setSubtitleOnClickListener
import io.plastique.core.extensions.setTitleOnClickListener
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.inject.getComponent
import io.plastique.watch.WatcherListEvent.LoadMoreEvent
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class WatcherListActivity : MvvmActivity<WatcherListViewModel>() {
    private lateinit var watchersView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var contentViewController: ContentViewController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: WatcherListAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    @Inject lateinit var navigator: WatchNavigator
    private lateinit var state: WatcherListViewState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watcher_list)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        initToolbar(username)

        adapter = WatcherListAdapter()
        adapter.onWatcherClickListener = { item -> navigator.openUserProfile(navigationContext, item.watcher.username) }

        onScrollListener = EndlessScrollListener(5) { viewModel.dispatch(LoadMoreEvent) }

        watchersView = findViewById(R.id.watchers)
        watchersView.layoutManager = LinearLayoutManager(this)
        watchersView.adapter = adapter
        watchersView.addOnScrollListener(onScrollListener)
        watchersView.addItemDecoration(DividerItemDecoration.Builder(this).build())

        refreshLayout = findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener {
            if (state.signInNeeded) {
                navigator.openLogin(navigationContext)
            } else {
                viewModel.dispatch(RetryClickEvent)
            }
        })

        contentViewController = ContentViewController(this, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)

        viewModel.init(username)
        viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second, it.third) }
                .disposeOnDestroy()
    }

    private fun renderState(state: WatcherListViewState, prevState: WatcherListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state

        contentViewController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
        }

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.isPagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun initToolbar(username: String?) {
        val toolbar = setActionBar(R.id.toolbar) {
            setTitle(if (username != null) R.string.watch_watchers_title else R.string.watch_watchers_title_current_user)
            subtitle = username
            setDisplayHomeAsUpEnabled(true)
        }

        val onClickListener = View.OnClickListener { watchersView.scrollToPosition(0) }
        toolbar.setTitleOnClickListener(onClickListener)
        toolbar.setSubtitleOnClickListener(onClickListener)
    }

    override fun injectDependencies() {
        getComponent<WatchActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_USERNAME = "username"

        fun createIntent(context: Context, username: String?): Intent {
            return Intent(context, WatcherListActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
        }
    }
}
