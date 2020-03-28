package io.plastique.statuses

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import io.plastique.comments.OnCommentsClickListener

class StatusActionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val commentsButton: TextView
    private val shareButton: View
    private lateinit var state: StatusActionsState

    var onCommentsClick: OnCommentsClickListener = {}
    var onShareClick: OnShareClickListener = {}

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_status_actions, this)

        commentsButton = findViewById(R.id.status_actions_comments)
        commentsButton.setOnClickListener { onCommentsClick(state.commentThreadId) }
        shareButton = findViewById(R.id.status_actions_share)
        shareButton.setOnClickListener { onShareClick(state.shareObjectId!!) }
    }

    fun render(state: StatusActionsState) {
        this.state = state
        commentsButton.text = state.commentCount.toString()
        shareButton.isVisible = state.shareObjectId != null
    }
}
