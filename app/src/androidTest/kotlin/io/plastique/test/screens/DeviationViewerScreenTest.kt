package io.plastique.test.screens

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.runner.screenshot.Screenshot
import io.plastique.R
import io.plastique.core.navigation.Route
import io.plastique.deviations.viewer.DeviationViewerActivity
import io.plastique.test.filter.GeneratesScreenshot
import io.plastique.test.rules.ScreenTestRule
import io.plastique.test.util.ScreenshotProcessor
import io.plastique.test.util.takeScreenshot
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeviationViewerScreenTest {
    @get:Rule
    val screenTestRule = ScreenTestRule()

    @Before
    fun setUp() {
        Screenshot.setScreenshotProcessors(setOf(ScreenshotProcessor))
    }

    @Test
    @GeneratesScreenshot
    fun displaysDeviation() {
        val route = DeviationViewerActivity.route(ApplicationProvider.getApplicationContext(), "63B2F441-CF22-9866-71A0-326888A3241C") as Route.Activity
        ActivityScenario.launch<DeviationViewerActivity>(route.intent).use {
            onView(withId(R.id.info_panel))
                .check(matches(isDisplayed()))
                .check(selectedDescendantsMatch(withId(R.id.title), withText("Goth Witch")))

            onIdle {
                takeScreenshot("deviation_viewer")
            }
        }
    }
}
