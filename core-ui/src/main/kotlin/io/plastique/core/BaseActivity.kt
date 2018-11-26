package io.plastique.core

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.plastique.core.config.AppConfig
import io.plastique.core.themes.ThemeManager
import io.plastique.inject.ActivityComponent
import io.plastique.inject.AppComponent
import io.plastique.inject.FragmentComponent
import io.plastique.inject.getComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), ActivityComponent.Holder, FragmentComponent.Factory {
    @Inject lateinit var appConfig: AppConfig
    @Inject lateinit var themeManager: ThemeManager

    private lateinit var currentTheme: String
    private val disposables = CompositeDisposable()
    private var themeDisposable: Disposable? = null

    protected abstract fun injectDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        currentTheme = themeManager.currentTheme
        themeManager.applyTheme(this, currentTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        themeDisposable = themeManager.themeChanges
                .filter { theme -> theme != currentTheme }
                .subscribe { theme ->
                    currentTheme = theme
                    recreate()
                }

        appConfig.fetch()
    }

    override fun onStop() {
        super.onStop()
        themeDisposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected fun <T : Disposable> T.disposeOnDestroy(): T {
        disposables.add(this)
        return this
    }

    override val activityComponent: ActivityComponent by lazy(LazyThreadSafetyMode.NONE) {
        @Suppress("DEPRECATION")
        (lastCustomNonConfigurationInstance as ActivityComponent?) ?: application.getComponent<AppComponent>().createActivityComponent()
    }

    override fun createFragmentComponent(): FragmentComponent {
        return activityComponent.createFragmentComponent()
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onRetainCustomNonConfigurationInstance(): Any = activityComponent
}
