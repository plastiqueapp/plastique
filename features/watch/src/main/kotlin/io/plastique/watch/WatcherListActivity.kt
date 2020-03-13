package io.plastique.watch

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import com.github.technoir42.glide.preloader.ListPreloader
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.inject.getComponent
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers

class WatcherListActivity : BaseActivity(R.layout.activity_watcher_list) {
    private val viewModel: WatcherListViewModel by viewModel()
    private val navigator: WatchNavigator get() = viewModel.navigator

    private lateinit var watchersView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: WatcherListAdapter
    private lateinit var onScrollListener: EndlessScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.attach(navigationContext)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        initToolbar(username)

        val imageLoader = ImageLoader.from(this)
        adapter = WatcherListAdapter(
            imageLoader = imageLoader,
            onUserClick = { user -> navigator.openUserProfile(user) })

        watchersView = findViewById(R.id.watchers)
        watchersView.layoutManager = LinearLayoutManager(this)
        watchersView.adapter = adapter
        createPreloader(imageLoader, adapter).attach(watchersView)

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(WatcherListEvent.LoadMoreEvent) }
        watchersView.addOnScrollListener(onScrollListener)

        refreshLayout = findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
            .disposeOnDestroy()
    }

    private fun renderState(state: WatcherListViewState, listUpdateData: ListUpdateData<ListItem>) {
        contentStateController.state = state.contentState
        emptyView.state = state.emptyState

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        refreshLayout.isRefreshing = state.listState.isRefreshing
        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    private fun createPreloader(imageLoader: ImageLoader, adapter: WatcherListAdapter): ListPreloader {
        val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_small)
        val callback = ListPreloader.Callback { position, preloader ->
            val item = adapter.items[position] as? WatcherItem ?: return@Callback
            val avatarUrl = item.watcher.user.avatarUrl
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
        private const val MAX_PRELOAD = 10

        fun route(context: Context, username: String?): Route = activityRoute<WatcherListActivity>(context) {
            putExtra(EXTRA_USERNAME, username)
        }
    }
}
