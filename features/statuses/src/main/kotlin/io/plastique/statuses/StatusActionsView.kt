package io.plastique.statuses

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible

class StatusActionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val commentsButton: TextView
    private val shareButton: View

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_status_actions, this)
        commentsButton = findViewById(R.id.status_actions_comments)
        shareButton = findViewById(R.id.status_actions_share)
    }

    fun render(state: StatusActionsState) {
        commentsButton.text = state.commentCount.toString()
        shareButton.isVisible = state.isShareEnabled
    }

    fun setOnCommentsClickListener(listener: OnClickListener) {
        commentsButton.setOnClickListener(listener)
    }

    fun setOnShareClickListener(listener: OnClickListener) {
        shareButton.setOnClickListener(listener)
    }
}
