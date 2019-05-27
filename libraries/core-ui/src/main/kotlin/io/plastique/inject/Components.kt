package io.plastique.inject

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.plastique.core.init.Initializer

interface BaseAppComponent : BaseActivityComponent.Factory {
    fun initializers(): Set<@JvmSuppressWildcards Initializer>

    interface Holder {
        val appComponent: BaseAppComponent
    }
}

interface BaseActivityComponent : BaseFragmentComponent.Factory {
    fun viewModelFactory(): ViewModelProvider.Factory

    interface Factory {
        fun createActivityComponent(): BaseActivityComponent
    }

    interface Holder {
        val activityComponent: BaseActivityComponent
    }
}

interface BaseFragmentComponent {
    fun viewModelFactory(): ViewModelProvider.Factory

    interface Factory {
        fun createFragmentComponent(): BaseFragmentComponent
    }

    interface Holder {
        val fragmentComponent: BaseFragmentComponent
    }
}

inline fun <reified T> Application.getComponent(): T =
    (this as BaseAppComponent.Holder).appComponent as T

inline fun <reified T> Activity.getComponent(): T =
    (this as BaseActivityComponent.Holder).activityComponent as T

inline fun <reified T> Fragment.getComponent(): T =
    (this as BaseFragmentComponent.Holder).fragmentComponent as T
