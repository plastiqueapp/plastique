package io.plastique.settings.licenses

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.settings.R
import io.plastique.settings.SettingsActivityComponent
import io.plastique.settings.SettingsNavigator
import io.plastique.settings.databinding.ActivityLicensesBinding
import io.plastique.settings.licenses.LicensesEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LicensesActivity : BaseActivity() {
    @Inject lateinit var navigator: SettingsNavigator

    private val viewModel: LicensesViewModel by viewModel()

    private lateinit var binding: ActivityLicensesBinding
    private lateinit var licensesAdapter: LicensesAdapter
    private lateinit var contentStateController: ContentStateController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLicensesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
        navigator.attach(navigationContext)

        licensesAdapter = LicensesAdapter(onLicenseClick = { license -> navigator.openUrl(license.url) })

        binding.licenses.apply {
            adapter = licensesAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration.Builder(context)
                .divider(R.drawable.preference_list_divider)
                .viewTypes(LicensesAdapter.TYPE_LICENSE)
                .build())
        }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, binding.licenses, binding.progress, binding.empty)

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
                licensesAdapter.update(state.items)
            }

            is LicensesViewState.Empty -> {
                contentStateController.state = ContentState.Empty
                binding.empty.state = state.emptyState
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
