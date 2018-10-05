package io.plastique.settings.about.licenses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.BrowserLauncher
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentViewController
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.inject.getComponent
import io.plastique.settings.R
import io.plastique.settings.SettingsActivityComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class LicensesActivity : MvvmActivity<LicensesViewModel>() {
    private lateinit var adapter: LicensesAdapter
    private lateinit var contentViewController: ContentViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = LicensesAdapter()
        adapter.onLicenseClickListener = { license -> BrowserLauncher(this).openUrl(license.url) }

        val licensesView = findViewById<RecyclerView>(R.id.licenses)
        licensesView.adapter = adapter
        licensesView.layoutManager = LinearLayoutManager(this)
        licensesView.addItemDecoration(DividerItemDecoration.Builder(this)
                .divider(R.drawable.preference_list_divider)
                .viewTypes(LicensesAdapter.TYPE_LICENSE)
                .build())

        contentViewController = ContentViewController(this, R.id.licenses, android.R.id.progress)

        observeState()
    }

    override fun injectDependencies() {
        getComponent<SettingsActivityComponent>().inject(this)
    }

    private fun observeState() {
        viewModel.state
                .map { state -> state.contentState }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentState -> contentViewController.switchState(contentState) }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.items }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items -> adapter.update(items) }
                .disposeOnDestroy()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LicensesActivity::class.java)
        }
    }
}
