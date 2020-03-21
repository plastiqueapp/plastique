package io.plastique.deviations.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.Checkable
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.comments.CommentThreadId
import io.plastique.comments.OnCommentsClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.OnFavoriteClickListener
import io.plastique.deviations.R
import io.plastique.deviations.databinding.ViewDeviationViewerInfoBinding
import io.plastique.users.OnUserClickListener

class InfoPanelView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding = ViewDeviationViewerInfoBinding.inflate(layoutInflater, this)
    private var state: InfoViewState? = null

    var onAuthorClick: OnUserClickListener = {}
    var onCommentsClick: OnCommentsClickListener = {}
    var onFavoriteClick: OnFavoriteClickListener = { _, _ -> }
    var onInfoClick: OnDeviationClickListener = {}

    init {
        val onAuthorClickListener = OnClickListener { onAuthorClick(state!!.author) }
        binding.authorName.setOnClickListener(onAuthorClickListener)
        binding.authorAvatar.setOnClickListener(onAuthorClickListener)
        binding.title.setOnClickListener(onAuthorClickListener)
        binding.comments.setOnClickListener { onCommentsClick(CommentThreadId.Deviation(state!!.deviationId)) }
        binding.favorite.setOnClickListener { view -> onFavoriteClick(state!!.deviationId, (view as Checkable).isChecked) }
        binding.info.setOnClickListener { onInfoClick(state!!.deviationId) }
    }

    fun render(state: InfoViewState, imageLoader: ImageLoader) {
        binding.title.text = state.title
        binding.authorAvatar.contentDescription = resources.getString(R.string.common_avatar_description, state.author.name)
        binding.authorName.text = state.author.name
        binding.favorite.apply {
            text = state.favoriteCount.formatCount()
            isChecked = state.isFavoriteChecked
            isEnabled = state.isFavoriteEnabled
            updateDrawablePadding()
        }
        binding.comments.apply {
            text = state.commentCount.formatCount()
            isEnabled = state.isCommentsEnabled
            updateDrawablePadding()
        }

        if (this.state?.author?.avatarUrl != state.author.avatarUrl) {
            imageLoader.load(state.author.avatarUrl)
                .params {
                    fallbackDrawable = R.drawable.default_avatar_64dp
                    transforms += TransformType.CircleCrop
                }
                .into(binding.authorAvatar)
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
