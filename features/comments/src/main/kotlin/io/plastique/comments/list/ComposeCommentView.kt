package io.plastique.comments.list

import android.content.Context
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.htmlEncode
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.comments.R
import io.plastique.comments.databinding.ViewCommentsComposeBinding

class ComposeCommentView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding = ViewCommentsComposeBinding.inflate(layoutInflater, this)

    var onCancelReplyClick: OnCancelReplyClickListener = {}
    var onPostCommentClick: OnPostCommentClickListener = {}
    var onSignInClick: OnSignInClickListener = {}

    var draft: CharSequence
        get() = binding.draft.text
        set(value) {
            binding.draft.setText(value)
        }

    var isPostingComment: Boolean
        get() = binding.postSwitcher.displayedChild == 1
        set(value) {
            binding.postSwitcher.displayedChild = if (value) 1 else 0
        }

    var isSignedIn: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                applySignedInState(value)
            }
        }

    var replyUsername: String? = null
        set(value) {
            if (field != value) {
                field = value
                applyReplyingState(value)
            }
        }

    init {
        binding.signIn.setOnClickListener { onSignInClick() }
        binding.postComment.setOnClickListener { onPostCommentClick(draft.toString()) }
        binding.postComment.isEnabled = false
        binding.cancelReply.setOnClickListener { onCancelReplyClick() }
        binding.draft.doAfterTextChanged { text -> binding.postComment.isEnabled = !text.isNullOrBlank() }

        applySignedInState(isSignedIn)
    }

    private fun applySignedInState(signedIn: Boolean) {
        binding.draft.isVisible = signedIn
        binding.postSwitcher.isVisible = signedIn
        binding.groupSignIn.isVisible = !signedIn

        binding.textReplyingTo.isVisible = signedIn && replyUsername != null
        binding.cancelReply.isVisible = signedIn && replyUsername != null
    }

    private fun applyReplyingState(username: String?) {
        val replying = username != null

        TransitionManager.beginDelayedTransition(this, REPLY_TRANSITION)
        binding.textReplyingTo.text = if (replying) {
            HtmlCompat.fromHtml(resources.getString(R.string.comments_compose_replying_to, username?.htmlEncode()), 0)
        } else {
            null
        }
        binding.textReplyingTo.isVisible = replying
        binding.cancelReply.isVisible = replying
    }

    companion object {
        private val REPLY_TRANSITION = object : TransitionSet() {
            init {
                ordering = ORDERING_TOGETHER
                addTransition(Fade(Fade.IN))
                addTransition(ChangeBounds())
            }
        }
    }
}

internal typealias OnCancelReplyClickListener = () -> Unit
internal typealias OnPostCommentClickListener = (commentText: String) -> Unit
internal typealias OnSignInClickListener = () -> Unit
