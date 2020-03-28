package io.plastique.deviations

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import io.plastique.comments.OnCommentsClickListener
import io.plastique.statuses.OnShareClickListener

class DeviationActionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val favoriteButton: CheckedTextView
    private val commentsButton: TextView
    private val shareButton: View
    private lateinit var state: DeviationActionsState

    var onCommentsClick: OnCommentsClickListener = {}
    var onFavoriteClick: OnFavoriteClickListener = { _, _ -> }
    var onShareClick: OnShareClickListener = {}

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_deviation_actions, this)

        favoriteButton = findViewById(R.id.deviation_actions_favorite)
        favoriteButton.setOnClickListener { onFavoriteClick(state.deviationId, favoriteButton.isChecked) }
        commentsButton = findViewById(R.id.deviation_actions_comments)
        commentsButton.setOnClickListener { onCommentsClick(state.commentThreadId!!) }
        shareButton = findViewById(R.id.deviation_actions_share)
        shareButton.setOnClickListener { onShareClick(state.shareObjectId) }
    }

    fun render(state: DeviationActionsState) {
        this.state = state
        favoriteButton.text = state.favoriteCount.toString()
        favoriteButton.isChecked = state.isFavorite
        commentsButton.text = state.commentCount.toString()
        commentsButton.isVisible = state.commentThreadId != null
    }
}
