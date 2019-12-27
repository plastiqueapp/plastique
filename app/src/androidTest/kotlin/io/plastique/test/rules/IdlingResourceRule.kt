package io.plastique.test.rules

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class IdlingResourceRule(private vararg val resources: IdlingResource) : TestRule {
    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                IdlingRegistry.getInstance().register(*resources)
                try {
                    base.evaluate()
                } finally {
                    IdlingRegistry.getInstance().unregister(*resources)
                }
            }
        }
    }
}
