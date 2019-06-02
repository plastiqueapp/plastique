package io.plastique.watch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.github.technoir42.kotlin.extensions.plus
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.setSubtitleOnClickListener
import io.plastique.core.extensions.setTitleOnClickListener
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.MvvmActivity
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.glide.RecyclerViewPreloader
import io.plastique.inject.getComponent
import io.plastique.util.Size
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class WatcherListActivity : MvvmActivity<WatcherListViewModel>(WatcherListViewModel::class.java) {
    @Inject lateinit var navigator: WatchNavigator

    private lateinit var watchersView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: WatcherListAdapter
    private lateinit var onScrollListener: EndlessScrollListener

    private lateinit var state: WatcherListViewState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watcher_list)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        initToolbar(username)

        val glide = GlideApp.with(this)
        adapter = WatcherListAdapter(
            glide = glide,
            onUserClick = { user -> navigator.openUserProfile(navigationContext, user) })

        watchersView = findViewById(R.id.watchers)
        watchersView.layoutManager = LinearLayoutManager(this)
        watchersView.adapter = adapter
        watchersView.addOnScrollListener(createPreloader(glide, adapter))

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(WatcherListEvent.LoadMoreEvent) }
        watchersView.addOnScrollListener(onScrollListener)

        refreshLayout = findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener {
            if (state.signInNeeded) {
                navigator.openLogin(navigationContext)
            } else {
                viewModel.dispatch(RetryClickEvent)
            }
        }

        contentStateController = ContentStateController(this, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)

        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.items, it.first.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second, it.third) }
            .disposeOnDestroy()
    }

    private fun renderState(state: WatcherListViewState, prevState: WatcherListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state

        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.state = state.contentState.emptyState
        }

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.isPagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun createPreloader(glide: GlideRequests, adapter: WatcherListAdapter): RecyclerView.OnScrollListener {
        val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_small)
        val preloadSize = Size(avatarSize, avatarSize)

        val callback = object : RecyclerViewPreloader.Callback<String> {
            override fun getPreloadItems(position: Int): List<String> {
                return when (val item = adapter.items[position]) {
                    is WatcherItem -> listOfNotNull(item.watcher.user.avatarUrl)
                    else -> emptyList()
                }
            }

            override fun createRequestBuilder(item: String): RequestBuilder<*> {
                return glide.load(item)
                    .circleCrop()
                    .dontAnimate()
                    .skipMemoryCache(true)
                    .priority(Priority.LOW)
            }

            override fun getPreloadSize(item: String): Size = preloadSize
        }

        return RecyclerViewPreloader(glide, lifecycle, callback, maxPreload = 10)
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
        private const val LOAD_MORE_THRESHOLD = 10

        fun createIntent(context: Context, username: String?): Intent {
            return Intent(context, WatcherListActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
        }
    }
}
