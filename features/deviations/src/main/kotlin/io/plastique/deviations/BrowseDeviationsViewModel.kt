package io.plastique.deviations

import io.plastique.core.ViewModel
import io.plastique.deviations.list.LayoutMode
import io.reactivex.Observable
import javax.inject.Inject

class BrowseDeviationsViewModel @Inject constructor(
    private val contentSettings: ContentSettings
) : ViewModel() {

    val layoutMode: Observable<LayoutMode>
        get() = contentSettings.layoutModeChanges

    fun setLayoutMode(layoutMode: LayoutMode) {
        contentSettings.layoutMode = layoutMode
    }
}
