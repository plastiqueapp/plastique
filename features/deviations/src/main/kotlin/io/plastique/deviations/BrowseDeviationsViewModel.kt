package io.plastique.deviations

import io.plastique.core.BaseViewModel
import io.plastique.deviations.list.LayoutMode
import io.plastique.inject.scopes.FragmentScope
import io.reactivex.Observable
import javax.inject.Inject

@FragmentScope
class BrowseDeviationsViewModel @Inject constructor(
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    val layoutMode: Observable<LayoutMode>
        get() = contentSettings.layoutModeChanges

    fun setLayoutMode(layoutMode: LayoutMode) {
        contentSettings.layoutMode = layoutMode
    }
}
