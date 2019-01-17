package io.plastique.deviations.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.plastique.deviations.Deviation
import io.plastique.deviations.R
import io.plastique.glide.GlideApp

class InfoPanelView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val titleView: TextView
    private val authorView: TextView
    private val avatarView: ImageView
    private val favoriteButton: CheckedTextView
    private val commentsButton: TextView
    private val infoButton: View

    private var avatarUrl: String? = null

    var isFavoriteEnabled: Boolean
        get() = favoriteButton.isEnabled
        set(value) {
            favoriteButton.isEnabled = value
        }

    init {
        inflate(context, R.layout.view_deviation_viewer_info, this)
        titleView = findViewById(R.id.deviation_title)
        authorView = findViewById(R.id.deviation_author)
        avatarView = findViewById(R.id.deviation_author_avatar)
        favoriteButton = findViewById(R.id.button_favorite)
        commentsButton = findViewById(R.id.button_comments)
        infoButton = findViewById(R.id.button_info)
    }

    fun render(deviation: Deviation) {
        titleView.text = deviation.title
        authorView.text = deviation.author.name
        favoriteButton.text = deviation.stats.favorites.formatCount()
        favoriteButton.isChecked = deviation.properties.isFavorite
        favoriteButton.updateDrawablePadding()
        commentsButton.text = deviation.stats.comments.formatCount()
        commentsButton.updateDrawablePadding()

        if (avatarUrl != deviation.author.avatarUrl) {
            avatarUrl = deviation.author.avatarUrl
            GlideApp.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .dontAnimate()
                    .into(avatarView)
        }
    }

    fun setOnAuthorClickListener(listener: (View) -> Unit) {
        authorView.setOnClickListener(listener)
        avatarView.setOnClickListener(listener)
        titleView.setOnClickListener(listener)
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
