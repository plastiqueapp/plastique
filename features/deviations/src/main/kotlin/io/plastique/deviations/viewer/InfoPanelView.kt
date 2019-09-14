package io.plastique.deviations.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.Checkable
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.plastique.deviations.R
import io.plastique.glide.GlideRequests
import io.plastique.users.User

class InfoPanelView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val titleView: TextView
    private val authorView: TextView
    private val avatarView: ImageView
    private val favoriteButton: CheckedTextView
    private val commentsButton: TextView
    private val infoButton: View

    private var author: User? = null

    init {
        inflate(context, R.layout.view_deviation_viewer_info, this)
        titleView = findViewById(R.id.deviation_title)
        authorView = findViewById(R.id.deviation_author)
        avatarView = findViewById(R.id.deviation_author_avatar)
        favoriteButton = findViewById(R.id.button_favorite)
        commentsButton = findViewById(R.id.button_comments)
        infoButton = findViewById(R.id.button_info)
    }

    fun render(state: InfoViewState, glide: GlideRequests) {
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

        if (author?.avatarUrl != state.author.avatarUrl) {
            glide.load(state.author.avatarUrl)
                .fallback(R.drawable.default_avatar_64dp)
                .circleCrop()
                .dontAnimate()
                .into(avatarView)
        }

        author = state.author
    }

    fun setOnAuthorClickListener(listener: (User) -> Unit) {
        val onClickListener = OnClickListener { listener(author!!) }
        authorView.setOnClickListener(onClickListener)
        avatarView.setOnClickListener(onClickListener)
        titleView.setOnClickListener(onClickListener)
    }

    fun setOnFavoriteClickListener(listener: (View, isChecked: Boolean) -> Unit) {
        favoriteButton.setOnClickListener { listener(it, (it as Checkable).isChecked) }
    }

    fun setOnCommentsClickListener(listener: (View) -> Unit) {
        commentsButton.setOnClickListener(listener)
    }

    fun setOnInfoClickListener(listener: (View) -> Unit) {
        infoButton.setOnClickListener(listener)
    }

    private fun Int.formatCount(): String {
        return if (this > 0) toString() else ""
    }

    private fun TextView.updateDrawablePadding() {
        compoundDrawablePadding = if (text.isNotEmpty()) resources.getDimensionPixelOffset(R.dimen.common_button_drawable_padding) else 0
    }
}
