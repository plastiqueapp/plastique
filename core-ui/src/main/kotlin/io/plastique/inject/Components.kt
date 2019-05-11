package io.plastique.inject

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment

interface AppComponent : ActivityComponent.Factory {
    interface Holder {
        val appComponent: AppComponent
    }
}

interface ActivityComponent : FragmentComponent.Factory {
    interface Factory {
        fun createActivityComponent(): ActivityComponent
    }

    interface Holder {
        val activityComponent: ActivityComponent
    }
}

interface FragmentComponent {
    interface Factory {
        fun createFragmentComponent(): FragmentComponent
    }

    interface Holder {
        val fragmentComponent: FragmentComponent
    }
}

inline fun <reified T> Application.getComponent(): T =
    (this as AppComponent.Holder).appComponent as T

inline fun <reified T> Activity.getComponent(): T =
    (this as ActivityComponent.Holder).activityComponent as T

inline fun <reified T> Fragment.getComponent(): T =
    (this as FragmentComponent.Holder).fragmentComponent as T
