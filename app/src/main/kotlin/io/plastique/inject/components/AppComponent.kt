package io.plastique.inject.components

import io.plastique.inject.BaseAppComponent

interface AppComponent : BaseAppComponent {
    override fun createActivityComponent(): ActivityComponent
}
