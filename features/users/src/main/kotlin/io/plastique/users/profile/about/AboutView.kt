package io.plastique.users.profile.about

import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.users.databinding.FragmentUsersAboutBinding

internal class AboutView(
    fragment: Fragment,
    private val binding: FragmentUsersAboutBinding,
    onRetryClick: () -> Unit
) : ScrollableToTop {

    private val contentStateController: ContentStateController

    init {
        binding.bio.movementMethod = LinkMovementMethod.getInstance()
        binding.empty.onButtonClick = onRetryClick

        contentStateController = ContentStateController(fragment, binding.content, binding.progress, binding.empty)
    }

    fun render(state: AboutViewState) = when (state) {
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

    override fun scrollToTop() {
        binding.content.smoothScrollTo(0, 0)
    }
}
