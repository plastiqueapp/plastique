package io.plastique.core.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

interface NavigationContext {
    val context: Context

    fun startActivity(intent: Intent, options: Bundle? = null)
}

private class ActivityNavigationContext(private val activity: Activity) : NavigationContext {
    override val context: Context
        get() = activity

    override fun startActivity(intent: Intent, options: Bundle?) {
        activity.startActivity(intent, options)
    }
}

private class FragmentNavigationContext(private val fragment: Fragment) : NavigationContext {
    override val context: Context
        get() = fragment.requireContext()

    override fun startActivity(intent: Intent, options: Bundle?) {
        fragment.startActivity(intent, options)
    }
}

val Activity.navigationContext: NavigationContext
    get() = ActivityNavigationContext(this)

val Fragment.navigationContext: NavigationContext
    get() = FragmentNavigationContext(this)
