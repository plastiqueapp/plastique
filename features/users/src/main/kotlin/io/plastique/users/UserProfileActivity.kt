package io.plastique.users

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.plastique.users.UserProfileEvent.CopyProfileLinkClickEvent
import io.plastique.users.UserProfileEvent.RetryClickEvent
import io.plastique.users.UserProfileEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class UserProfileActivity : MvvmActivity<UserProfileViewModel>() {
    private lateinit var rootView: View
    private lateinit var avatarView: ImageView
    private lateinit var realNameView: TextView
    private lateinit var emptyView: EmptyView
    private lateinit var contentViewController: ContentViewController
    private lateinit var snackbarController: SnackbarController
    @Inject lateinit var navigator: UsersNavigator

    private var showMenu: Boolean = false
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
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        rootView = findViewById(android.R.id.content)
        avatarView = findViewById(R.id.user_avatar)
        realNameView = findViewById(R.id.user_real_name)

        contentViewController = ContentViewController(this, R.id.profile_content, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(rootView)

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener { viewModel.dispatch(RetryClickEvent) })

        viewModel.init(username)
        viewModel.state
                .pairwiseWithPrevious()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second) }
                .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (showMenu) {
            menuInflater.inflate(R.menu.activity_user_profile, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.users_action_view_collections -> {
            navigator.openCollections(navigationContext, username)
            true
        }
        R.id.users_action_view_gallery -> {
            navigator.openGallery(navigationContext, username)
            true
        }
        R.id.users_action_view_comments -> {
            navigator.openComments(navigationContext, CommentThreadId.Profile(username))
            true
        }
        R.id.users_action_copy_profile_link -> {
            viewModel.dispatch(CopyProfileLinkClickEvent)
            true
        }
        R.id.users_action_view_watchers -> {
            navigator.openWatchers(navigationContext, username)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun renderState(state: UserProfileViewState, prevState: UserProfileViewState?) {
        supportActionBar!!.title = state.title

        contentViewController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
        }

        if (state.userProfile != prevState?.userProfile) {
            realNameView.text = state.userProfile!!.realName

            GlideApp.with(this)
                    .load(state.userProfile.user.avatarUrl)
                    .circleCrop()
                    .into(avatarView)

            showMenu = true
            invalidateOptionsMenu()
        }

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
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
