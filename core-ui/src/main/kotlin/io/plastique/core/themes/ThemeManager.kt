package io.plastique.core.themes

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.annotation.StyleRes
import io.plastique.util.Preferences
import io.reactivex.Observable
import javax.inject.Inject

class ThemeManager @Inject constructor(private val preferences: Preferences) {
    @Theme
    val currentTheme: String
        get() {
            val theme = preferences.getString(PREF_UI_THEME, Themes.DEFAULT)
            return ensureValid(theme)
        }

    val themeChanges: Observable<String>
        get() = preferences.observable().getString(PREF_UI_THEME, Themes.DEFAULT)
                .map { theme -> ensureValid(theme) }
                .distinctUntilChanged()

    fun applyTheme(activity: Activity, @Theme theme: String) {
        val themeResId = getThemeResourceId(activity, theme)
        activity.setTheme(themeResId)

        // Workaround for a bug where android:windowLightStatusBar is not applied correctly after changing theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val a = activity.obtainStyledAttributes(intArrayOf(android.R.attr.windowLightStatusBar))
            val windowLightStatusBar = a.getBoolean(0, false)
            a.recycle()

            var flags = activity.window.decorView.systemUiVisibility
            flags = if (windowLightStatusBar) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            activity.window.decorView.systemUiVisibility = flags
        }
    }

    @StyleRes
    private fun getThemeResourceId(activity: Activity, theme: String): Int {
        val activityInfo = try {
            activity.packageManager.getActivityInfo(activity.componentName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }

        if (theme == Themes.DARK) {
            if (activityInfo.metaData != null) {
                val darkTheme = activityInfo.metaData.getInt(META_DARK_THEME)
                if (darkTheme != 0) {
                    return darkTheme
                }
            }
            if (activityInfo.applicationInfo.metaData != null) {
                val darkTheme = activityInfo.applicationInfo.metaData.getInt(META_DARK_THEME)
                if (darkTheme != 0) {
                    return darkTheme
                }
            }
        }
        return activityInfo.themeResource
    }

    private fun ensureValid(theme: String): String {
        return if (Themes.isValid(theme)) theme else Themes.DEFAULT
    }

    companion object {
        private const val META_DARK_THEME = "darkTheme"
        private const val PREF_UI_THEME = "ui.theme"
    }
}
