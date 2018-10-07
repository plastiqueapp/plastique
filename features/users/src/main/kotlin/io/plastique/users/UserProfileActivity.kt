package io.plastique.users

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.navigation.navigationContext
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.plastique.users.UserProfileEvent.CopyProfileLinkClickEvent
import io.plastique.users.UserProfileEvent.LinkCopiedMessageShownEvent
import io.plastique.users.UserProfileEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class UserProfileActivity : MvvmActivity<UserProfileViewModel>() {
    private lateinit var rootView: View
    private lateinit var avatarView: ImageView
    private lateinit var realNameView: TextView
    private lateinit var emptyView: EmptyView
    private lateinit var contentViewController: ContentViewController
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

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener { viewModel.dispatch(RetryClickEvent) })

        viewModel.init(username)
        observeState()
    }

    private fun observeState() {
        viewModel.state
                .map { state -> state.contentState }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentState ->
                    contentViewController.switchState(contentState)
                    if (contentState is ContentState.Empty) {
                        emptyView.setState(contentState.emptyState)
                    }
                }
                .disposeOnDestroy()

        viewModel.state
                .filter { state -> state.userProfile != null }
                .map { state -> state.userProfile!! }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { userProfile ->
                    showMenu = true
                    realNameView.text = userProfile.realName

                    GlideApp.with(this)
                            .load(userProfile.user.avatarUrl)
                            .circleCrop()
                            .into(avatarView)

                    invalidateOptionsMenu()
                }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.title }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { title -> supportActionBar!!.title = title }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.showLinkCopiedMessage }
                .filter { state -> state.showLinkCopiedMessage }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Snackbar.make(rootView, R.string.common_message_link_copied, Snackbar.LENGTH_SHORT).show()
                    viewModel.dispatch(LinkCopiedMessageShownEvent)
                }
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
            navigator.openCommentsForUserProfile(navigationContext, username)
            true
        }
        R.id.users_action_copy_profile_link -> {
            viewModel.dispatch(CopyProfileLinkClickEvent)
            true
        }
        else -> super.onOptionsItemSelected(item)
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
