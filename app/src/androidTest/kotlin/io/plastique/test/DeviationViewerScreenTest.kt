package io.plastique.test

import android.Manifest
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.screenshot.Screenshot
import io.plastique.deviations.viewer.DeviationViewerActivity
import io.plastique.inject.components.AppComponent
import io.plastique.inject.getComponent
import io.plastique.test.util.IdlingResourceRule
import io.plastique.test.util.OkHttp3IdlingResource
import io.plastique.test.util.ScreenshotProcessor
import io.plastique.test.util.takeScreenshot
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeviationViewerScreenTest {
    private val appComponent: AppComponent get() = ApplicationProvider.getApplicationContext<Application>().getComponent()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val idlingResourceRule = IdlingResourceRule(OkHttp3IdlingResource(appComponent.okHttpClient(), "OkHttpClient"))

    @Before
    fun setUp() {
        Screenshot.setScreenshotProcessors(setOf(ScreenshotProcessor))
    }

    @Test
    fun screenshot() {
        val intent = DeviationViewerActivity.createIntent(ApplicationProvider.getApplicationContext(), "63B2F441-CF22-9866-71A0-326888A3241C")
        ActivityScenario.launch<DeviationViewerActivity>(intent)

        Thread.sleep(4000)
        onIdle()
        takeScreenshot("deviation_viewer")
    }
}
