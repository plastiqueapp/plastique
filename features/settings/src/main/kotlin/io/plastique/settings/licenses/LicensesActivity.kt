package io.plastique.settings.licenses

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.inject.getComponent
import io.plastique.settings.R
import io.plastique.settings.SettingsActivityComponent
import io.plastique.settings.SettingsNavigator
import io.plastique.settings.licenses.LicensesEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LicensesActivity : BaseActivity() {
    @Inject lateinit var navigator: SettingsNavigator

    private val viewModel: LicensesViewModel by viewModel()

    private lateinit var emptyView: EmptyView
    private lateinit var adapter: LicensesAdapter
    private lateinit var contentStateController: ContentStateController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        adapter = LicensesAdapter(onLicenseClick = { license -> navigator.openUrl(license.url) })

        val licensesView = findViewById<RecyclerView>(R.id.licenses)
        licensesView.adapter = adapter
        licensesView.layoutManager = LinearLayoutManager(this)
        licensesView.addItemDecoration(DividerItemDecoration.Builder(this)
            .divider(R.drawable.preference_list_divider)
            .viewTypes(LicensesAdapter.TYPE_LICENSE)
            .build())

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, R.id.licenses, android.R.id.progress, android.R.id.empty)

        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it) }
            .disposeOnDestroy()
    }

    private fun renderState(state: LicensesViewState) {
        when (state) {
            LicensesViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is LicensesViewState.Content -> {
                contentStateController.state = ContentState.Content
                adapter.update(state.items)
            }

            is LicensesViewState.Empty -> {
                contentStateController.state = ContentState.Empty
                emptyView.state = state.emptyState
            }
        }
    }

    override fun injectDependencies() {
        getComponent<SettingsActivityComponent>().inject(this)
    }

    companion object {
        fun route(context: Context): Route = activityRoute<LicensesActivity>(context)
    }
}
