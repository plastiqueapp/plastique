package io.plastique.settings.licenses

import android.content.Context
import android.os.Bundle
import io.plastique.core.BaseActivity
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.settings.SettingsActivityComponent
import io.plastique.settings.SettingsNavigator
import io.plastique.settings.licenses.LicensesEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LicensesActivity : BaseActivity() {
    @Inject lateinit var navigator: SettingsNavigator

    private val viewModel: LicensesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.attach(navigationContext)

        val view = LicensesView(
            this,
            onRetryClick = { viewModel.dispatch(RetryClickEvent) },
            onLicenseClick = { license -> navigator.openUrl(license.url) })

        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(it) }
            .disposeOnDestroy()
    }

    override fun injectDependencies() {
        getComponent<SettingsActivityComponent>().inject(this)
    }

    companion object {
        fun route(context: Context): Route = activityRoute<LicensesActivity>(context)
    }
}
