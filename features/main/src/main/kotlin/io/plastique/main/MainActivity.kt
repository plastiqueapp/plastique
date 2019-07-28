package io.plastique.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.github.technoir42.android.extensions.disableDragging
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setTitleOnClickListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.mvvm.MvvmActivity
import io.plastique.core.navigation.navigationContext
import io.plastique.glide.CustomDrawableTarget
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.inject.getComponent
import io.plastique.util.InstantAppHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainActivity : MvvmActivity<MainViewModel>(MainViewModel::class.java),
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener {

    @Inject lateinit var mainFragmentFactory: MainFragmentFactory
    @Inject lateinit var navigator: MainNavigator
    @Inject lateinit var instantAppHelper: InstantAppHelper

    private val glide: GlideRequests by lazy(LazyThreadSafetyMode.NONE) { GlideApp.with(this) }
    private lateinit var expandableToolbarLayout: ExpandableToolbarLayout

    private lateinit var state: MainViewState

    private val fragmentLifecycleCallbacks = object : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fragmentManager: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment is MainPage) {
                onPageCreated(fragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigator.attach(navigationContext)

        val toolbar = setActionBar(R.id.toolbar)
        toolbar.setTitleOnClickListener(View.OnClickListener { scrollToTop() })

        val appBar = findViewById<AppBarLayout>(R.id.appbar)
        appBar.disableDragging()
        expandableToolbarLayout = findViewById(R.id.expandable_toolbar)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.setOnNavigationItemReselectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.tab_content, mainFragmentFactory.createFragment(this, supportFragmentManager.fragmentFactory, R.id.main_tab_browse))
                .commit()
        }

        viewModel.init()
        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state -> renderState(state) }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val installFullMenuItem = menu.findItem(R.id.main_action_install_full_version)
        installFullMenuItem.isVisible = instantAppHelper.isInstantApp
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            state.user?.let { navigator.openUserProfile(it) }
            true
        }
        R.id.main_action_settings -> {
            navigator.openSettings()
            true
        }
        R.id.main_action_install_full_version -> {
            instantAppHelper.showInstallPrompt(this)
            true
        }
        else -> false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tab_content, mainFragmentFactory.createFragment(this, supportFragmentManager.fragmentFactory, item.itemId))
            .commit()
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        scrollToTop()
    }

    private fun renderState(state: MainViewState) {
        this.state = state

        if (state.user != null) {
            val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_small)
            glide.load(state.user.avatarUrl)
                .placeholder(R.drawable.default_avatar_32dp)
                .error(R.drawable.default_avatar_32dp)
                .circleCrop()
                .into(object : CustomDrawableTarget(avatarSize, avatarSize) {
                    override fun setDrawable(drawable: Drawable?) {
                        setCurrentUserIcon(drawable)
                    }
                })
        } else {
            setCurrentUserIcon(null)
        }
    }

    private fun scrollToTop() {
        val fragment = supportFragmentManager.findFragmentById(R.id.tab_content)
        if (fragment is ScrollableToTop) {
            fragment.scrollToTop()
        }
    }

    override fun injectDependencies() {
        getComponent<MainActivityComponent>().inject(this)
    }

    private fun onPageCreated(page: MainPage) {
        supportActionBar?.setTitle(page.getTitle())
        if (expandableToolbarLayout.childCount > 1) {
            expandableToolbarLayout.removeViews(1, expandableToolbarLayout.childCount - 1)
        }
        page.createAppBarViews(expandableToolbarLayout)
    }

    private fun setCurrentUserIcon(drawable: Drawable?) {
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(drawable != null)
            setHomeAsUpIndicator(drawable)
        }
    }
}
