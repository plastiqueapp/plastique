package io.plastique.test.screens

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.screenshot.Screenshot
import io.plastique.core.themes.ThemeManager
import io.plastique.inject.components.AppComponent
import io.plastique.inject.getComponent
import io.plastique.main.MainActivity
import io.plastique.test.filter.GeneratesScreenshot
import io.plastique.test.rules.IdlingResourceRule
import io.plastique.test.util.OkHttp3IdlingResource
import io.plastique.test.util.ScreenshotProcessor
import io.plastique.test.util.takeScreenshot
import io.plastique.util.Preferences
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BrowseScreenTest {
    private val appComponent: AppComponent get() = ApplicationProvider.getApplicationContext<Application>().getComponent()
    private val preferences: Preferences get() = appComponent.preferences()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val idlingResourceRule = IdlingResourceRule(OkHttp3IdlingResource(appComponent.okHttpClient(), "OkHttpClient"))

    @Before
    fun setUp() {
        Screenshot.setScreenshotProcessors(setOf(ScreenshotProcessor))
    }

    @After
    fun tearDown() {
        preferences.edit { remove(ThemeManager.PREF_UI_THEME) }
    }

    @Test
    @GeneratesScreenshot
    fun screenshot() {
        ActivityScenario.launch(MainActivity::class.java)

        Thread.sleep(4000)
        onIdle()
        takeScreenshot("browse")
    }

    @Test
    @GeneratesScreenshot
    fun screenshotDark() {
        preferences.edit { put(ThemeManager.PREF_UI_THEME, ThemeManager.THEME_DARK) }
        ActivityScenario.launch(MainActivity::class.java)

        Thread.sleep(4000)
        onIdle()
        takeScreenshot("browse_dark")
    }
}
