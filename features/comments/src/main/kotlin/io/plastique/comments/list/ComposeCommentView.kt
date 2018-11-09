package io.plastique.comments.list

import android.content.Context
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
import io.plastique.comments.R
import io.plastique.core.extensions.doAfterTextChanged
import io.plastique.util.HtmlCompat

class ComposeCommentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val draftView: EditText
    private val replyingToView: TextView
    private val cancelReplyButton: Button
    private val postButton: ImageButton
    private val signInGroup: View
    private val postProgressSwitcher: ViewSwitcher
    private var replying: Boolean = false
    var onCancelReplyClickListener: OnCancelReplyClickListener? = null
    var onPostCommentListener: OnPostCommentListener? = null
    var onSignInClickListener: OnSignInClickListener? = null

    init {
        View.inflate(context, R.layout.view_comments_compose, this)

        draftView = findViewById(R.id.comment_draft)
        postProgressSwitcher = findViewById(R.id.comment_post_switcher)
        postButton = findViewById(R.id.button_post_comment)
        signInGroup = findViewById(R.id.group_sign_in)

        replyingToView = findViewById(R.id.text_replying_to)
        cancelReplyButton = findViewById(R.id.button_cancel_reply)

        draftView.doAfterTextChanged { text -> postButton.isEnabled = text.isNotEmpty() }
        postButton.isEnabled = false

        val signInButton = findViewById<Button>(R.id.button_sign_in)
        signInButton.setOnClickListener { onSignInClickListener?.invoke() }
        postButton.setOnClickListener { onPostCommentListener?.invoke(draftView.text.toString()) }
        cancelReplyButton.setOnClickListener { onCancelReplyClickListener?.invoke() }
    }

    fun setDraft(draft: String) {
        draftView.setText(draft)
    }

    fun setReplyUserName(username: String?) {
        TransitionManager.beginDelayedTransition(this, REPLY_TRANSITION)

        if (username != null) {
            replying = true
            replyingToView.text = formatReplyingToText(username)
            replyingToView.visibility = View.VISIBLE
            cancelReplyButton.visibility = View.VISIBLE
        } else {
            replying = false
            replyingToView.visibility = View.GONE
            cancelReplyButton.visibility = View.GONE
        }
    }

    fun setPosting(posting: Boolean) {
        postProgressSwitcher.displayedChild = if (posting) 1 else 0
    }

    fun showSignIn() {
        signInGroup.visibility = View.VISIBLE
        replyingToView.visibility = View.GONE
        cancelReplyButton.visibility = View.GONE

        draftView.visibility = View.GONE
        postProgressSwitcher.visibility = View.GONE
    }

    fun showCompose() {
        signInGroup.visibility = View.GONE
        draftView.visibility = View.VISIBLE
        postProgressSwitcher.visibility = View.VISIBLE

        if (replying) {
            replyingToView.visibility = View.VISIBLE
            cancelReplyButton.visibility = View.VISIBLE
        }
    }

    private fun formatReplyingToText(username: String): CharSequence {
        return HtmlCompat.fromHtml(resources.getString(R.string.comments_compose_replying_to, username))
    }

    companion object {
        private val REPLY_TRANSITION = object : TransitionSet() {
            init {
                ordering = TransitionSet.ORDERING_TOGETHER
                addTransition(Fade(Fade.IN))
                addTransition(ChangeBounds())
            }
        }
    }
}

typealias OnCancelReplyClickListener = () -> Unit
typealias OnPostCommentListener = (commentText: String) -> Unit
typealias OnSignInClickListener = () -> Unit
