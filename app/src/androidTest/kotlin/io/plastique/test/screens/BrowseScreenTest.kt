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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BrowseScreenTest {
    @get:Rule
    val screenTestRule = ScreenTestRule()

    private val themeManager = screenTestRule.appComponent.themeManager()

    @Before
    fun setUp() {
        Screenshot.setScreenshotProcessors(setOf(ScreenshotProcessor))
    }

    @Test
    @GeneratesScreenshot
    fun screenshotLight() {
        themeManager.currentTheme = ThemeManager.THEME_LIGHT

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
        themeManager.currentTheme = ThemeManager.THEME_DARK

        ActivityScenario.launch(MainActivity::class.java).use {
            // Wait for images to load
            Thread.sleep(4000)
            onIdle {
                takeScreenshot("browse_dark")
            }
        }
    }
}
