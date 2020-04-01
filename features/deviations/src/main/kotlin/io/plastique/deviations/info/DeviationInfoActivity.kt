package io.plastique.deviations.info

import android.content.Context
import android.os.Bundle
import io.plastique.core.BaseActivity
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.info.DeviationInfoEvent.RetryClickEvent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class DeviationInfoActivity : BaseActivity() {
    @Inject lateinit var navigator: DeviationsNavigator

    private val viewModel: DeviationInfoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.attach(navigationContext)

        val view = DeviationInfoView(
            this,
            onRetryClick = { viewModel.dispatch(RetryClickEvent) },
            onTagClick = { tag -> navigator.openTag(tag) })

        val deviationId = intent.getStringExtra(EXTRA_DEVIATION_ID)!!
        viewModel.init(deviationId)
        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(it) }
            .disposeOnDestroy()
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_DEVIATION_ID = "deviation_id"

        fun route(context: Context, deviationId: String): Route = activityRoute<DeviationInfoActivity>(context) {
            putExtra(EXTRA_DEVIATION_ID, deviationId)
        }
    }
}
