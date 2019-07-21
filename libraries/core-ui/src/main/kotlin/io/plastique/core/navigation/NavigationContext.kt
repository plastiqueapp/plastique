package io.plastique.core.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

interface NavigationContext {
    val context: Context
    val fragmentManager: FragmentManager

    fun startActivity(intent: Intent, options: Bundle? = null)
}

private class ActivityNavigationContext(private val activity: FragmentActivity) : NavigationContext {
    override val context: Context get() = activity
    override val fragmentManager: FragmentManager get() = activity.supportFragmentManager

    override fun startActivity(intent: Intent, options: Bundle?) {
        activity.startActivity(intent, options)
    }
}

private class FragmentNavigationContext(private val fragment: Fragment) : NavigationContext {
    override val context: Context get() = fragment.requireContext()
    override val fragmentManager: FragmentManager get() = fragment.childFragmentManager

    override fun startActivity(intent: Intent, options: Bundle?) {
        fragment.startActivity(intent, options)
    }
}

val FragmentActivity.navigationContext: NavigationContext
    get() = ActivityNavigationContext(this)

val Fragment.navigationContext: NavigationContext
    get() = FragmentNavigationContext(this)
