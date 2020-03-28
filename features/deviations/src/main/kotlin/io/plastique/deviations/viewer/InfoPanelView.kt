package io.plastique.deviations.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.plastique.comments.CommentThreadId
import io.plastique.comments.OnCommentsClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.OnFavoriteClickListener
import io.plastique.deviations.R
import io.plastique.users.OnUserClickListener

class InfoPanelView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val titleView: TextView
    private val authorView: TextView
    private val avatarView: ImageView
    private val favoriteButton: CheckedTextView
    private val commentsButton: TextView
    private val infoButton: View

    private var state: InfoViewState? = null

    var onAuthorClick: OnUserClickListener = {}
    var onCommentsClick: OnCommentsClickListener = {}
    var onFavoriteClick: OnFavoriteClickListener = { _, _ -> }
    var onInfoClick: OnDeviationClickListener = {}

    init {
        inflate(context, R.layout.view_deviation_viewer_info, this)
        titleView = findViewById(R.id.deviation_title)
        authorView = findViewById(R.id.deviation_author)
        avatarView = findViewById(R.id.deviation_author_avatar)
        favoriteButton = findViewById(R.id.button_favorite)
        commentsButton = findViewById(R.id.button_comments)
        infoButton = findViewById(R.id.button_info)

        val onAuthorClickListener = OnClickListener { onAuthorClick(state!!.author) }
        authorView.setOnClickListener(onAuthorClickListener)
        avatarView.setOnClickListener(onAuthorClickListener)
        titleView.setOnClickListener(onAuthorClickListener)
        commentsButton.setOnClickListener { onCommentsClick(CommentThreadId.Deviation(state!!.deviationId)) }
        favoriteButton.setOnClickListener { onFavoriteClick(state!!.deviationId, favoriteButton.isChecked) }
        infoButton.setOnClickListener { onInfoClick(state!!.deviationId) }
    }

    fun render(state: InfoViewState, imageLoader: ImageLoader) {
        titleView.text = state.title
        avatarView.contentDescription = resources.getString(R.string.common_avatar_description, state.author.name)
        authorView.text = state.author.name
        favoriteButton.text = state.favoriteCount.formatCount()
        favoriteButton.isChecked = state.isFavoriteChecked
        favoriteButton.isEnabled = state.isFavoriteEnabled
        favoriteButton.updateDrawablePadding()
        commentsButton.text = state.commentCount.formatCount()
        commentsButton.isEnabled = state.isCommentsEnabled
        commentsButton.updateDrawablePadding()

        if (this.state?.author?.avatarUrl != state.author.avatarUrl) {
            imageLoader.load(state.author.avatarUrl)
                .params {
                    fallbackDrawable = R.drawable.default_avatar_64dp
                    transforms += TransformType.CircleCrop
                }
                .into(avatarView)
        }
        this.state = state
    }

    private fun Int.formatCount(): String {
        return if (this > 0) toString() else ""
    }

    private fun TextView.updateDrawablePadding() {
        compoundDrawablePadding = if (text.isNotEmpty()) resources.getDimensionPixelOffset(R.dimen.common_button_drawable_padding) else 0
    }
}
