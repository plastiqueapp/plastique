package io.plastique.users.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.FragmentListPagerAdapter
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.extensions.doOnTabReselected
import io.plastique.core.extensions.setActionBar
import io.plastique.core.mvvm.MvvmActivity
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.plastique.users.R
import io.plastique.users.UsersActivityComponent
import io.plastique.users.UsersNavigator
import io.plastique.users.profile.UserProfileEvent.CopyProfileLinkClickEvent
import io.plastique.users.profile.UserProfileEvent.RetryClickEvent
import io.plastique.users.profile.UserProfileEvent.SetWatchingEvent
import io.plastique.users.profile.UserProfileEvent.SignOutEvent
import io.plastique.users.profile.UserProfileEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class UserProfileActivity : MvvmActivity<UserProfileViewModel>(UserProfileViewModel::class.java), CompoundButton.OnCheckedChangeListener {
    @Inject lateinit var navigator: UsersNavigator
    @Inject lateinit var pageProvider: UserProfilePageProvider

    private lateinit var rootView: View
    private lateinit var avatarView: ImageView
    private lateinit var realNameView: TextView
    private lateinit var statisticsView: UserStatisticsView
    private lateinit var watchButton: ToggleButton
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController

    private lateinit var state: UserProfileViewState

    private val username: String by lazy(LazyThreadSafetyMode.NONE) {
        if (intent.hasExtra(EXTRA_USERNAME)) {
            intent.getStringExtra(EXTRA_USERNAME)
        } else {
            val host = intent.data?.host!!
            host.substring(0, host.indexOf("."))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        setHasOptionsMenu(false)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
        initTabs()

        rootView = findViewById(android.R.id.content)
        avatarView = findViewById(R.id.user_avatar)
        realNameView = findViewById(R.id.user_real_name)

        statisticsView = findViewById(R.id.statistics)
        statisticsView.setOnWatchersClickListener(View.OnClickListener { navigator.openWatchers(navigationContext, username) })

        watchButton = findViewById(R.id.button_watch)
        watchButton.setOnCheckedChangeListener(this)

        contentStateController = ContentStateController(this, R.id.profile_content, android.R.id.progress, android.R.id.empty)
        progressDialogController = ProgressDialogController(this, supportFragmentManager)
        snackbarController = SnackbarController(rootView)

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second) }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_user_profile, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.users_profile_action_sign_out)?.isVisible = state.isCurrentUser
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.users_profile_action_copy_profile_link -> {
            viewModel.dispatch(CopyProfileLinkClickEvent)
            true
        }
        R.id.users_profile_action_open_in_browser -> {
            navigator.openUrl(navigationContext, state.userProfile!!.url)
            true
        }
        R.id.users_profile_action_sign_out -> {
            viewModel.dispatch(SignOutEvent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView === watchButton) {
            // Restore isChecked value because it should be driven by state
            buttonView.setOnCheckedChangeListener(null)
            buttonView.isChecked = state.userProfile?.isWatching == true
            buttonView.setOnCheckedChangeListener(this)

            if (state.isSignedIn) {
                viewModel.dispatch(SetWatchingEvent(isChecked))
            } else {
                navigator.openLogin(navigationContext)
            }
        }
    }

    private fun renderState(state: UserProfileViewState, prevState: UserProfileViewState?) {
        this.state = state
        supportActionBar!!.title = state.title
        setHasOptionsMenu(state.userProfile != null)

        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.state = state.contentState.emptyState
        }

        if (state.userProfile != prevState?.userProfile) {
            realNameView.text = state.userProfile!!.realName
            statisticsView.render(state.userProfile.stats)

            watchButton.setOnCheckedChangeListener(null)
            watchButton.isChecked = state.userProfile.isWatching
            watchButton.setOnCheckedChangeListener(this)

            GlideApp.with(this)
                .load(state.userProfile.user.avatarUrl)
                .fallback(R.drawable.default_avatar_64dp)
                .circleCrop()
                .dontAnimate()
                .into(avatarView)
        }

        watchButton.isVisible = state.contentState === ContentState.Content && !state.isCurrentUser
        progressDialogController.isShown = state.showProgressDialog

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun initTabs() {
        val adapter = FragmentListPagerAdapter(this, pageProvider.getPages(username))
        val pager: ViewPager = findViewById(R.id.pager)
        pager.adapter = adapter
        pager.pageMargin = resources.getDimensionPixelOffset(R.dimen.users_profile_page_spacing)

        val tabLayout: TabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(pager)
        tabLayout.doOnTabReselected { tab ->
            val fragment = adapter.getFragment(tab.position)
            if (fragment is ScrollableToTop) {
                fragment.scrollToTop()
            }
        }
    }

    override fun injectDependencies() {
        getComponent<UsersActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_USERNAME = "username"

        fun createIntent(context: Context, username: String): Intent {
            return Intent(context, UserProfileActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
        }
    }
}
