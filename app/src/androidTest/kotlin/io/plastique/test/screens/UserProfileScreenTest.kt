package io.plastique.test.screens

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.runner.screenshot.Screenshot
import io.plastique.core.navigation.Route
import io.plastique.test.filter.GeneratesScreenshot
import io.plastique.test.rules.ScreenTestRule
import io.plastique.test.util.ScreenshotProcessor
import io.plastique.test.util.takeScreenshot
import io.plastique.users.profile.UserProfileActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserProfileScreenTest {
    @get:Rule
    val screenTestRule = ScreenTestRule()

    @Before
    fun setUp() {
        Screenshot.setScreenshotProcessors(setOf(ScreenshotProcessor))
    }

    @Test
    @GeneratesScreenshot
    fun galleryScreenshot() {
        val route = UserProfileActivity.route(ApplicationProvider.getApplicationContext(), "rossdraws") as Route.Activity
        ActivityScenario.launch<UserProfileActivity>(route.intent).use {
            Thread.sleep(2000)
            onView(withText("Gallery")).perform(click())

            // Wait for images to load
            Thread.sleep(4000)
            onIdle {
                takeScreenshot("user_profile")
            }
        }
    }
}
