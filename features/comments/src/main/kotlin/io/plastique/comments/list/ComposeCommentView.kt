package io.plastique.comments.list

import android.content.Context
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.plastique.comments.R

class ComposeCommentView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val draftView: EditText
    private val replyingToView: TextView
    private val cancelReplyButton: Button
    private val postButton: ImageButton
    private val signInGroup: View
    private val postProgressSwitcher: ViewSwitcher
    var onCancelReplyClickListener: OnCancelReplyClickListener? = null
    var onPostCommentListener: OnPostCommentListener? = null
    var onSignInClickListener: OnSignInClickListener? = null

    var draft: CharSequence
        get() = draftView.text
        set(value) {
            draftView.setText(value)
        }

    var isPostingComment: Boolean
        get() = postProgressSwitcher.displayedChild == 1
        set(value) {
            postProgressSwitcher.displayedChild = if (value) 1 else 0
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
        View.inflate(context, R.layout.view_comments_compose, this)

        postButton = findViewById(R.id.button_post_comment)
        postButton.setOnClickListener { onPostCommentListener?.invoke(draft.toString()) }
        postButton.isEnabled = false
        postProgressSwitcher = findViewById(R.id.comment_post_switcher)

        draftView = findViewById(R.id.comment_draft)
        draftView.doAfterTextChanged { text -> postButton.isEnabled = !text.isNullOrBlank() }

        replyingToView = findViewById(R.id.text_replying_to)
        cancelReplyButton = findViewById(R.id.button_cancel_reply)
        cancelReplyButton.setOnClickListener { onCancelReplyClickListener?.invoke() }

        signInGroup = findViewById(R.id.group_sign_in)
        val signInButton: Button = findViewById(R.id.button_sign_in)
        signInButton.setOnClickListener { onSignInClickListener?.invoke() }

        applySignedInState(isSignedIn)
    }

    private fun applySignedInState(signedIn: Boolean) {
        draftView.isVisible = signedIn
        postProgressSwitcher.isVisible = signedIn
        signInGroup.isVisible = !signedIn

        replyingToView.isVisible = signedIn && replyUsername != null
        cancelReplyButton.isVisible = signedIn && replyUsername != null
    }

    private fun applyReplyingState(username: String?) {
        val replying = username != null

        TransitionManager.beginDelayedTransition(this, REPLY_TRANSITION)
        replyingToView.text = if (replying) HtmlCompat.fromHtml(resources.getString(R.string.comments_compose_replying_to,
            TextUtils.htmlEncode(username)), 0) else null
        replyingToView.isVisible = replying
        cancelReplyButton.isVisible = replying
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

typealias OnCancelReplyClickListener = () -> Unit
typealias OnPostCommentListener = (commentText: String) -> Unit
typealias OnSignInClickListener = () -> Unit
