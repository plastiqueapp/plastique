package io.plastique.watch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.setSubtitleOnClickListener
import io.plastique.core.extensions.setTitleOnClickListener
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListItemDiffTransformer
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.watch.WatcherListEvent.LoadMoreEvent
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class WatcherListActivity : MvvmActivity<WatcherListViewModel>() {
    private lateinit var contentViewController: ContentViewController
    private lateinit var watchersView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var adapter: WatcherListAdapter
    private lateinit var endlessScrollListener: EndlessScrollListener
    @Inject lateinit var navigator: WatchNavigator
    private lateinit var state: WatcherListViewState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watcher_list)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        initToolbar(username)

        adapter = WatcherListAdapter()
        adapter.onWatcherClickListener = { item -> navigator.openUserProfile(navigationContext, item.watcher.username) }

        endlessScrollListener = EndlessScrollListener(5) { viewModel.dispatch(LoadMoreEvent) }

        watchersView = findViewById(R.id.watchers)
        watchersView.layoutManager = LinearLayoutManager(this)
        watchersView.adapter = adapter
        watchersView.addOnScrollListener(endlessScrollListener)
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

        viewModel.init(username)
        observeState()
    }

    private fun observeState() {
        viewModel.state
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> this.state = state }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.contentState }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentState ->
                    contentViewController.switchState(contentState)
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
                .map { state -> state.pagingEnabled }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pagingEnabled -> endlessScrollListener.enabled = pagingEnabled }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.refreshing }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { refreshing -> refreshLayout.isRefreshing = refreshing }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.snackbarMessage }
                .filter { state -> state.snackbarMessage != null }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    Snackbar.make(refreshLayout, state.snackbarMessage!!, Snackbar.LENGTH_SHORT).show()
                    viewModel.dispatch(SnackbarShownEvent)
                }
                .disposeOnDestroy()
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
