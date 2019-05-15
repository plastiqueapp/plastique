package io.plastique.core

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.plastique.core.config.AppConfig
import io.plastique.core.themes.ThemeId
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

    private val disposables = CompositeDisposable()
    private var themeDisposable: Disposable? = null
    private var currentTheme: ThemeId? = null
    private var hasMenu: Boolean = true
    protected var optionsMenu: Menu? = null

    protected abstract fun injectDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        themeManager.currentTheme.let { themeId ->
            currentTheme = themeId
            themeManager.applyTheme(this, themeId)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        themeDisposable = themeManager.themeChanges
            .filter { theme -> theme != currentTheme }
            .subscribe { theme ->
                currentTheme = theme
                ActivityCompat.recreate(this)
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

    final override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (hasMenu) {
            optionsMenu = menu
            onCreateOptionsMenu(menu, menuInflater)
        } else {
            optionsMenu = null
        }
        return super.onCreateOptionsMenu(menu)
    }

    open fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun setHasOptionsMenu(hasMenu: Boolean) {
        if (this.hasMenu != hasMenu) {
            this.hasMenu = hasMenu
            invalidateOptionsMenu()
        }
    }

    protected fun <T : Disposable> T.disposeOnDestroy(): T {
        disposables.add(this)
        return this
    }

    override val activityComponent: ActivityComponent by lazy(LazyThreadSafetyMode.NONE) {
        @Suppress("DEPRECATION")
        lastCustomNonConfigurationInstance as ActivityComponent?
            ?: application.getComponent<AppComponent>().createActivityComponent()
    }

    override fun createFragmentComponent(): FragmentComponent {
        return activityComponent.createFragmentComponent()
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onRetainCustomNonConfigurationInstance(): Any = activityComponent
}