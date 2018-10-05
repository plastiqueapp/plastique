package io.plastique.deviations.list

import io.plastique.deviations.DailyParams
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.inject.getComponent

class DailyDeviationsFragment : BaseDeviationListFragment<DailyParams>() {
    override val defaultParams get() = DailyParams()

    override fun injectDependencies() {
        getComponent<DeviationsFragmentComponent>().inject(this)
    }
}
