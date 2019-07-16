package io.plastique.deviations

import io.plastique.deviations.browse.BrowseDeviationsFragment
import io.plastique.deviations.list.DailyDeviationsFragment
import io.plastique.deviations.list.HotDeviationsFragment
import io.plastique.deviations.list.PopularDeviationsFragment
import io.plastique.deviations.list.UndiscoveredDeviationsFragment

interface DeviationsFragmentComponent {
    fun inject(fragment: BrowseDeviationsFragment)

    fun inject(fragment: DailyDeviationsFragment)

    fun inject(fragment: HotDeviationsFragment)

    fun inject(fragment: PopularDeviationsFragment)

    fun inject(fragment: UndiscoveredDeviationsFragment)
}
