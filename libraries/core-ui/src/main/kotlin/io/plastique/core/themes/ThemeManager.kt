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
    val currentTheme: ThemeId
        get() {
            val themeId = preferences.get(PREF_UI_THEME, THEME_DEFAULT)
            return themeId.ensureValid()
        }

    val themeChanges: Observable<ThemeId>
        get() = preferences.observable().get(PREF_UI_THEME, THEME_DEFAULT)
            .map { it.ensureValid() }
            .distinctUntilChanged()

    fun applyTheme(activity: Activity, themeId: ThemeId) {
        val themeResId = getThemeResourceId(activity, themeId)
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
    private fun getThemeResourceId(activity: Activity, themeId: ThemeId): Int {
        val activityInfo = activity.packageManager.getActivityInfo(activity.componentName, PackageManager.GET_META_DATA)
        if (themeId == THEME_DARK) {
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

    private fun ThemeId.ensureValid(): ThemeId {
        return if (this in VALID_THEMES) this else THEME_DEFAULT
    }

    companion object {
        private const val META_DARK_THEME = "darkTheme"
        private const val PREF_UI_THEME = "ui.theme"

        private val THEME_DARK = ThemeId("dark")
        private val THEME_LIGHT = ThemeId("light")
        private val THEME_DEFAULT = THEME_LIGHT
        private val VALID_THEMES = arrayOf(THEME_DARK, THEME_LIGHT)
    }
}
