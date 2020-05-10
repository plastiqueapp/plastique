package io.plastique.users.profile

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.core.view.isVisible
import com.github.technoir42.android.extensions.doOnTabReselected
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.core.pager.FragmentListPagerAdapter
import io.plastique.core.snackbar.SnackbarController
import io.plastique.inject.getComponent
import io.plastique.users.R
import io.plastique.users.UsersActivityComponent
import io.plastique.users.databinding.ActivityUserProfileBinding
import io.plastique.users.profile.UserProfileEvent.CopyProfileLinkClickEvent
import io.plastique.users.profile.UserProfileEvent.OpenInBrowserEvent
import io.plastique.users.profile.UserProfileEvent.RetryClickEvent
import io.plastique.users.profile.UserProfileEvent.SetWatchingEvent
import io.plastique.users.profile.UserProfileEvent.SignOutEvent
import io.plastique.users.profile.UserProfileEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class UserProfileActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    @Inject lateinit var pageProvider: UserProfilePageProvider

    private val imageLoader = ImageLoader.from(this)
    private val viewModel: UserProfileViewModel by viewModel()

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController

    private var showSignOut: Boolean = false

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
        viewModel.navigator.attach(navigationContext)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHasOptionsMenu(false)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
        initTabs()

        binding.statistics.onWatchersClick = { viewModel.navigator.openWatchers(username) }
        binding.watch.setOnCheckedChangeListener(this)
        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, binding.profileContent, binding.progress, binding.empty)
        progressDialogController = ProgressDialogController(this, supportFragmentManager)
        snackbarController = SnackbarController(binding.root)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

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
        menu.findItem(R.id.users_profile_action_sign_out)?.isVisible = showSignOut
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.users_profile_action_copy_profile_link -> {
            viewModel.dispatch(CopyProfileLinkClickEvent)
            true
        }
        R.id.users_profile_action_open_in_browser -> {
            viewModel.dispatch(OpenInBrowserEvent)
            true
        }
        R.id.users_profile_action_sign_out -> {
            viewModel.dispatch(SignOutEvent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView === binding.watch) {
            // Restore isChecked value because it should be driven by state
            buttonView.setOnCheckedChangeListener(null)
            buttonView.isChecked = buttonView.getTag(R.id.tag_is_checked) as Boolean
            buttonView.setOnCheckedChangeListener(this)

            viewModel.dispatch(SetWatchingEvent(isChecked))
        }
    }

    private fun renderState(state: UserProfileViewState, prevState: UserProfileViewState?) {
        supportActionBar!!.title = state.title
        setHasOptionsMenu(state.userProfile != null)
        showSignOut = state.showSignOut

        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState

        if (state.userProfile != null && state.userProfile != prevState?.userProfile) {
            binding.userAvatar.contentDescription = getString(R.string.common_avatar_description, state.userProfile.user.name)
            binding.userRealName.text = state.userProfile.realName
            binding.statistics.render(state.userProfile.stats)

            binding.watch.apply {
                setOnCheckedChangeListener(null)
                isChecked = state.userProfile.isWatching
                setTag(R.id.tag_is_checked, state.userProfile.isWatching)
                setOnCheckedChangeListener(this@UserProfileActivity)
            }

            imageLoader.load(state.userProfile.user.avatarUrl)
                .params {
                    fallbackDrawable = R.drawable.default_avatar_64dp
                    transforms += TransformType.CircleCrop
                }
                .into(binding.userAvatar)
        }

        binding.watch.isVisible = state.contentState == ContentState.Content && !state.isCurrentUser
        progressDialogController.isShown = state.showProgressDialog
        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    private fun initTabs() {
        val adapter = FragmentListPagerAdapter(this, pageProvider.getPages(username))
        binding.pager.adapter = adapter
        binding.pager.pageMargin = resources.getDimensionPixelOffset(R.dimen.users_profile_page_spacing)
        binding.tabs.setupWithViewPager(binding.pager)
        binding.tabs.doOnTabReselected { tab ->
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

        fun route(context: Context, username: String): Route = activityRoute<UserProfileActivity>(context) {
            putExtra(EXTRA_USERNAME, username)
        }
    }
}
