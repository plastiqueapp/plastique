package io.plastique.core

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.plastique.core.config.AppConfig
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseAppComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), BaseActivityComponent.Holder, BaseFragmentComponent.Factory {
    @Inject protected lateinit var appConfig: AppConfig

    private val disposables = CompositeDisposable()
    private var hasMenu: Boolean = true
    protected var optionsMenu: Menu? = null

    protected abstract fun injectDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        appConfig.fetch()
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

    override val activityComponent: BaseActivityComponent by lazy(LazyThreadSafetyMode.NONE) {
        @Suppress("DEPRECATION")
        lastCustomNonConfigurationInstance as BaseActivityComponent?
            ?: application.getComponent<BaseAppComponent>().createActivityComponent()
    }

    override fun createFragmentComponent(): BaseFragmentComponent {
        return activityComponent.createFragmentComponent()
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onRetainCustomNonConfigurationInstance(): Any = activityComponent
}
