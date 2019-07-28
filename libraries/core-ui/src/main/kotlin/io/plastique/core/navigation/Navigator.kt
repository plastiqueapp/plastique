package io.plastique.core.navigation

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

interface Navigator {
    fun attach(context: NavigationContext)

    fun navigateTo(route: Route)
}

abstract class BaseNavigator : Navigator {
    private var navigationContext: NavigationContext? = null
    private val queue = mutableListOf<Route>()
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun attach(context: NavigationContext) {
        check(navigationContext == null) { "Already attached to NavigationContext $context" }

        context.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                navigationContext = context
                processPendingRoutes(context)
            }

            override fun onStop(owner: LifecycleOwner) {
                navigationContext = null
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun navigateTo(route: Route) {
        if (Looper.myLooper() !== Looper.getMainLooper()) {
            mainThreadHandler.post { navigateTo(route) }
            return
        }

        val context = navigationContext
        if (context != null) {
            Timber.tag(LOG_TAG).d("Navigating to $route")
            context.navigateTo(route)
        } else {
            queue += route
        }
    }

    private fun processPendingRoutes(navigationContext: NavigationContext) {
        queue.forEach { route -> navigationContext.navigateTo(route) }
        queue.clear()
    }

    companion object {
        private const val LOG_TAG = "Navigation"
    }
}
