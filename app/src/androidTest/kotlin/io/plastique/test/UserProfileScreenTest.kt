package io.plastique.test

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.screenshot.Screenshot
import io.plastique.core.navigation.Route
import io.plastique.inject.components.AppComponent
import io.plastique.inject.getComponent
import io.plastique.test.util.IdlingResourceRule
import io.plastique.test.util.OkHttp3IdlingResource
import io.plastique.test.util.ScreenshotProcessor
import io.plastique.test.util.takeScreenshot
import io.plastique.users.profile.UserProfileActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserProfileScreenTest {
    private val appComponent: AppComponent get() = ApplicationProvider.getApplicationContext<Application>().getComponent()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val idlingResourceRule = IdlingResourceRule(OkHttp3IdlingResource(appComponent.okHttpClient(), "OkHttpClient"))

    @Before
    fun setUp() {
        Screenshot.setScreenshotProcessors(setOf(ScreenshotProcessor))
    }

    @Test
    fun screenshot() {
        val route = UserProfileActivity.route(ApplicationProvider.getApplicationContext(), "rossdraws") as Route.Activity
        ActivityScenario.launch<UserProfileActivity>(route.intent)

        Thread.sleep(2000)
        onView(withText("Gallery")).perform(click())
        Thread.sleep(4000)
        onIdle()
        takeScreenshot("user_profile")
    }
}
