package io.plastique.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import io.plastique.core.browser.BrowserLauncher

interface NavigationContext {
    val lifecycle: Lifecycle

    fun navigateTo(route: Route)
}

val FragmentActivity.navigationContext: NavigationContext
    get() = ActivityNavigationContext(this)

val Fragment.navigationContext: NavigationContext
    get() = FragmentNavigationContext(this)

private class ActivityNavigationContext(private val activity: FragmentActivity) : NavigationContext {
    override val lifecycle: Lifecycle get() = activity.lifecycle

    override fun navigateTo(route: Route) {
        when (route) {
            is Route.Activity -> activity.startActivity(route.intent)
            is Route.ActivityWithResult -> activity.startActivityForResult(route.intent, route.requestCode)
            is Route.Url -> BrowserLauncher().openUrl(activity, route.url)
        }
    }

    override fun toString(): String = "ActivityNavigationContext(${activity.javaClass.name})"
}

private class FragmentNavigationContext(private val fragment: Fragment) : NavigationContext {
    override val lifecycle: Lifecycle get() = fragment.lifecycle

    override fun navigateTo(route: Route) {
        when (route) {
            is Route.Activity -> fragment.startActivity(route.intent)
            is Route.ActivityWithResult -> fragment.startActivityForResult(route.intent, route.requestCode)
            is Route.Url -> BrowserLauncher().openUrl(fragment.requireContext(), route.url)
        }
    }

    override fun toString(): String = "FragmentNavigationContext(${fragment.javaClass.name})"
}
