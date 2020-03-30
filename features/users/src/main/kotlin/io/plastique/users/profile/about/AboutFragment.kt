package io.plastique.users.profile.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.plastique.core.BaseFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.mvvm.viewModel
import io.plastique.inject.getComponent
import io.plastique.users.UsersFragmentComponent
import io.plastique.users.UsersNavigator
import io.plastique.users.databinding.FragmentUsersAboutBinding
import io.plastique.users.profile.about.AboutEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class AboutFragment : BaseFragment(), ScrollableToTop {
    @Inject lateinit var navigator: UsersNavigator

    private val viewModel: AboutViewModel by viewModel()

    private lateinit var binding: FragmentUsersAboutBinding
    private lateinit var contentStateController: ContentStateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUsersAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bio.movementMethod = LinkMovementMethod.getInstance()
        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, binding.content, binding.progress, binding.empty)

        val username = requireArguments().getString(ARG_USERNAME)!!
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
                binding.bio.text = state.bio.value
            }

            is AboutViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is AboutViewState.Error -> {
                contentStateController.state = ContentState.Empty
                binding.empty.state = state.emptyState
            }
        }
    }

    override fun scrollToTop() {
        binding.content.smoothScrollTo(0, 0)
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
