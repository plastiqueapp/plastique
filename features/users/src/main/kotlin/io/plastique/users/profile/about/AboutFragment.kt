package io.plastique.users.profile.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.args
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.text.RichTextView
import io.plastique.inject.getComponent
import io.plastique.users.R
import io.plastique.users.UsersFragmentComponent
import io.plastique.users.UsersNavigator
import io.plastique.users.profile.about.AboutEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class AboutFragment : MvvmFragment<AboutViewModel>(AboutViewModel::class.java), ScrollableToTop {
    @Inject lateinit var navigator: UsersNavigator

    private lateinit var contentView: NestedScrollView
    private lateinit var bioHeaderView: View
    private lateinit var bioView: RichTextView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_users_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentView = view.findViewById(R.id.content)
        bioHeaderView = view.findViewById(R.id.bio_header)
        bioView = view.findViewById(R.id.bio)
        bioView.movementMethod = LinkMovementMethod.getInstance()

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(view, R.id.content, android.R.id.progress, android.R.id.empty)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val username = args.getString(ARG_USERNAME)!!
        viewModel.init(username)
        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it) }
            .disposeOnDestroy()
    }

    private fun renderState(state: AboutViewState) {
        when (state) {
            is AboutViewState.Content -> {
                contentStateController.state = ContentState.Content
                bioView.text = state.bio.value
            }

            is AboutViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is AboutViewState.Error -> {
                contentStateController.state = ContentState.Empty(state.emptyState)
                emptyView.state = state.emptyState
            }
        }
    }

    override fun scrollToTop() {
        contentView.smoothScrollTo(0, 0)
    }

    override fun injectDependencies() {
        getComponent<UsersFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_USERNAME = "username"

        fun newArgs(username: String): Bundle {
            return Bundle().apply {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
