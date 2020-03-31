package io.plastique.watch

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import com.github.technoir42.glide.preloader.ListPreloader
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.OnButtonClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.OnLoadMoreListener
import io.plastique.core.snackbar.OnSnackbarShownListener
import io.plastique.core.snackbar.SnackbarController
import io.plastique.users.OnUserClickListener
import io.plastique.watch.databinding.ActivityWatcherListBinding

internal class WatcherListView(
    private val activity: AppCompatActivity,
    onLoadMore: OnLoadMoreListener,
    onRetryClick: OnButtonClickListener,
    onRefresh: OnRefreshListener,
    onSnackbarShown: OnSnackbarShownListener,
    onUserClick: OnUserClickListener
) {
    private val imageLoader = ImageLoader.from(activity)
    private val binding = ActivityWatcherListBinding.inflate(activity.layoutInflater)
    private val watcherListAdapter: WatcherListAdapter
    private val onScrollListener: EndlessScrollListener
    private val contentStateController: ContentStateController
    private val snackbarController: SnackbarController

    init {
        activity.setContentView(binding.root)
        activity.setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        val onClickListener = View.OnClickListener { binding.watchers.scrollToPosition(0) }
        binding.toolbar.setTitleOnClickListener(onClickListener)
        binding.toolbar.setSubtitleOnClickListener(onClickListener)

        watcherListAdapter = WatcherListAdapter(imageLoader, onUserClick)
        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD, onLoadMore)

        binding.watchers.apply {
            adapter = watcherListAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }

        contentStateController = ContentStateController(activity, binding.refresh, binding.progress, binding.empty)
        snackbarController = SnackbarController(binding.refresh)
        snackbarController.onSnackbarShown = onSnackbarShown

        binding.empty.onButtonClick = onRetryClick
        binding.refresh.setOnRefreshListener(onRefresh)

        createPreloader()
    }

    fun setUsername(username: String?) {
        activity.supportActionBar!!.apply {
            setTitle(if (username != null) R.string.watch_watchers_title else R.string.watch_watchers_title_current_user)
            subtitle = username
        }
    }

    fun render(state: WatcherListViewState, listUpdateData: ListUpdateData<ListItem>) {
        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState
        binding.refresh.isRefreshing = state.listState.isRefreshing
        onScrollListener.isEnabled = state.listState.isPagingEnabled
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(watcherListAdapter)
    }

    private fun createPreloader() {
        val avatarSize = activity.resources.getDimensionPixelSize(R.dimen.common_avatar_size_small)
        val callback = ListPreloader.Callback { position, preloader ->
            val item = watcherListAdapter.items[position] as? WatcherItem
                ?: return@Callback
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

        ListPreloader(imageLoader.glide, callback, MAX_PRELOAD).attach(binding.watchers)
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 10
        private const val MAX_PRELOAD = 10
    }
}
