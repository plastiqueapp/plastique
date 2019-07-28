package io.plastique.core.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent

sealed class Route {
    data class Activity(val intent: Intent) : Route()
    data class ActivityWithResult(val intent: Intent, val requestCode: Int) : Route()
    data class Url(val url: String) : Route()
}

inline fun <reified T : Activity> activityRoute(context: Context, intentConfig: Intent.() -> Unit = {}): Route =
    Route.Activity(intent = Intent(context, T::class.java).apply(intentConfig))

inline fun <reified T : Activity> activityRoute(context: Context, requestCode: Int, intentConfig: Intent.() -> Unit = {}): Route =
    Route.ActivityWithResult(intent = Intent(context, T::class.java).apply(intentConfig), requestCode = requestCode)
