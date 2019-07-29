package io.plastique.core.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment

sealed class Route {
    data class Activity(val intent: Intent) : Route()
    data class ActivityWithResult(val intent: Intent, val requestCode: Int) : Route()
    data class Dialog(val fragmentClass: Class<out DialogFragment>, val tag: String, val args: Bundle? = null) : Route()
    data class Url(val url: String) : Route()
}

inline fun <reified T : Activity> activityRoute(context: Context, intentConfig: Intent.() -> Unit = {}): Route =
    Route.Activity(intent = Intent(context, T::class.java).apply(intentConfig))

inline fun <reified T : Activity> activityRoute(context: Context, requestCode: Int, intentConfig: Intent.() -> Unit = {}): Route =
    Route.ActivityWithResult(intent = Intent(context, T::class.java).apply(intentConfig), requestCode = requestCode)

inline fun <reified T : DialogFragment> dialogRoute(tag: String, args: Bundle? = null): Route =
    Route.Dialog(fragmentClass = T::class.java, tag = tag, args = args)
