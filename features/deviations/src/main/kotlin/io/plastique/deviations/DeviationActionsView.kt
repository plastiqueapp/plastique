package io.plastique.deviations

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible

class DeviationActionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val favoriteButton: CheckedTextView
    private val commentsButton: TextView
    private val shareButton: View

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_deviation_actions, this)
        favoriteButton = findViewById(R.id.deviation_actions_favorite)
        commentsButton = findViewById(R.id.deviation_actions_comments)
        shareButton = findViewById(R.id.deviation_actions_share)
    }

    fun render(state: DeviationActionsState) {
        favoriteButton.text = state.favoriteCount.toString()
        favoriteButton.isChecked = state.isFavorite
        commentsButton.text = state.commentCount.toString()
        commentsButton.isVisible = state.isCommentsEnabled
    }

    fun setOnFavoriteClickListener(listener: OnClickListener) {
        favoriteButton.setOnClickListener(listener)
    }

    fun setOnCommentsClickListener(listener: OnClickListener) {
        commentsButton.setOnClickListener(listener)
    }

    fun setOnShareClickListener(listener: OnClickListener) {
        shareButton.setOnClickListener(listener)
    }
}
