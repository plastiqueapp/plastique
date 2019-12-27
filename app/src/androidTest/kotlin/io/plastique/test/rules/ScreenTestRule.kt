package io.plastique.test.rules

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import io.plastique.inject.components.AppComponent
import io.plastique.inject.getComponent
import io.plastique.test.util.OkHttp3IdlingResource
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ScreenTestRule : TestRule {
    val appComponent: AppComponent
        get() = ApplicationProvider.getApplicationContext<Application>().getComponent()

    override fun apply(base: Statement, description: Description?): Statement {
        val rules = listOf(
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            IdlingResourceRule(OkHttp3IdlingResource(appComponent.okHttpClient(), "OkHttpClient"))
        )
        return rules.fold(base) { statement, rule -> rule.apply(statement, description) }
    }
}
