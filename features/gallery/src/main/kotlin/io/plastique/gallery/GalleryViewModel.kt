package io.plastique.gallery

import io.plastique.core.ViewModel
import io.plastique.inject.scopes.FragmentScope
import javax.inject.Inject

@FragmentScope
class GalleryViewModel @Inject constructor() : ViewModel() {
    fun init(username: String?) {
    }
}
