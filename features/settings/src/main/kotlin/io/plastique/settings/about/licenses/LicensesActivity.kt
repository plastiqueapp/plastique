package io.plastique.settings.about.licenses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BrowserLauncher
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentViewController
import io.plastique.core.extensions.add
import io.plastique.core.extensions.setActionBar
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.inject.getComponent
import io.plastique.settings.R
import io.plastique.settings.SettingsActivityComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LicensesActivity : MvvmActivity<LicensesViewModel>() {
    private lateinit var adapter: LicensesAdapter
    private lateinit var contentViewController: ContentViewController
    @Inject lateinit var browserLauncher: BrowserLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        adapter = LicensesAdapter(
                onLicenseClick = { license -> browserLauncher.openUrl(this, license.url) })

        val licensesView = findViewById<RecyclerView>(R.id.licenses)
        licensesView.adapter = adapter
        licensesView.layoutManager = LinearLayoutManager(this)
        licensesView.addItemDecoration(DividerItemDecoration.Builder(this)
                .divider(R.drawable.preference_list_divider)
                .viewTypes(LicensesAdapter.TYPE_LICENSE)
                .build())

        contentViewController = ContentViewController(this, R.id.licenses, android.R.id.progress)

        viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.third) }
                .disposeOnDestroy()
    }

    private fun renderState(state: LicensesViewState, listUpdateData: ListUpdateData<ListItem>) {
        contentViewController.state = state.contentState
        listUpdateData.applyTo(adapter)
    }

    override fun injectDependencies() {
        getComponent<SettingsActivityComponent>().inject(this)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LicensesActivity::class.java)
        }
    }
}
