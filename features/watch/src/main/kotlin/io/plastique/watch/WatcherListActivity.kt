package io.plastique.watch

import android.content.Context
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.watch.WatcherListEvent.LoadMoreEvent
import io.plastique.watch.WatcherListEvent.RefreshEvent
import io.plastique.watch.WatcherListEvent.RetryClickEvent
import io.plastique.watch.WatcherListEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers

class WatcherListActivity : BaseActivity() {
    private val viewModel: WatcherListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator.attach(navigationContext)

        val view = WatcherListView(
            this,
            onLoadMore = { viewModel.dispatch(LoadMoreEvent) },
            onRefresh = OnRefreshListener { viewModel.dispatch(RefreshEvent) },
            onRetryClick = { viewModel.dispatch(RetryClickEvent) },
            onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) },
            onUserClick = { viewModel.navigator.openUserProfile(it) }
        )

        val username = intent.getStringExtra(EXTRA_USERNAME)
        view.setUsername(username)

        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(it.first, it.third) }
            .disposeOnDestroy()
    }

    override fun injectDependencies() {
        getComponent<WatchActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_USERNAME = "username"

        fun route(context: Context, username: String?): Route = activityRoute<WatcherListActivity>(context) {
            putExtra(EXTRA_USERNAME, username)
        }
    }
}
