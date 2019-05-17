package io.plastique.core.themes

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import io.plastique.util.Preferences
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(private val preferences: Preferences) {
    @SuppressLint("CheckResult")
    fun init() {
        currentThemeChanges.subscribe { applyTheme(it) }
    }

    private fun applyTheme(themeId: ThemeId) {
        Timber.d("Applying theme '${themeId.value}'")
        AppCompatDelegate.setDefaultNightMode(themeId.toNightMode())
    }

    private val currentThemeChanges: Observable<ThemeId>
        get() = preferences.observable().get(PREF_UI_THEME, THEME_DEFAULT)
            .map { it.ensureValid() }
            .distinctUntilChanged()

    private fun ThemeId.ensureValid(): ThemeId {
        return if (this in VALID_THEMES) this else THEME_DEFAULT
    }

    private fun ThemeId.toNightMode(): Int = when (this) {
        THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
        THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }

    companion object {
        private const val PREF_UI_THEME = "ui.theme"

        private val THEME_DARK = ThemeId("dark")
        private val THEME_LIGHT = ThemeId("light")
        private val THEME_DEFAULT = ThemeId("default")
        private val VALID_THEMES = arrayOf(THEME_DARK, THEME_LIGHT, THEME_DEFAULT)
    }
}
