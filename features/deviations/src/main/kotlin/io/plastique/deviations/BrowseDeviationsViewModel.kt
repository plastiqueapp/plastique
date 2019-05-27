package io.plastique.deviations

import io.plastique.core.mvvm.BaseViewModel
import io.plastique.deviations.list.LayoutMode
import io.reactivex.Observable
import javax.inject.Inject

class BrowseDeviationsViewModel @Inject constructor(
    private val contentSettings: ContentSettings
) : BaseViewModel() {

    val layoutMode: Observable<LayoutMode>
        get() = contentSettings.layoutModeChanges

    fun setLayoutMode(layoutMode: LayoutMode) {
        contentSettings.layoutMode = layoutMode
    }
}
