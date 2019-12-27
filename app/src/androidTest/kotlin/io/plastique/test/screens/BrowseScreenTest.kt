package io.plastique.test.screens

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.runner.screenshot.Screenshot
import io.plastique.core.themes.ThemeManager
import io.plastique.main.MainActivity
import io.plastique.test.filter.GeneratesScreenshot
import io.plastique.test.rules.ScreenTestRule
import io.plastique.test.util.ScreenshotProcessor
import io.plastique.test.util.takeScreenshot
import io.plastique.util.Preferences
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BrowseScreenTest {
    @get:Rule
    val screenTestRule = ScreenTestRule()

    private val preferences: Preferences get() = screenTestRule.appComponent.preferences()

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
        ActivityScenario.launch(MainActivity::class.java).use {
            // Wait for images to load
            Thread.sleep(4000)

            onIdle {
                takeScreenshot("browse")
            }
        }
    }

    @Test
    @GeneratesScreenshot
    fun screenshotDark() {
        preferences.edit { put(ThemeManager.PREF_UI_THEME, ThemeManager.THEME_DARK) }
        ActivityScenario.launch(MainActivity::class.java).use {
            // Wait for images to load
            Thread.sleep(4000)
            onIdle {
                takeScreenshot("browse_dark")
            }
        }
    }
}
