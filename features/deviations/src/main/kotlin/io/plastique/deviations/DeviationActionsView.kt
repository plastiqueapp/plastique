package io.plastique.deviations

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.comments.OnCommentsClickListener
import io.plastique.deviations.databinding.ViewDeviationActionsBinding
import io.plastique.statuses.OnShareClickListener

class DeviationActionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val binding = ViewDeviationActionsBinding.inflate(layoutInflater, this)
    private lateinit var state: DeviationActionsState

    var onCommentsClick: OnCommentsClickListener = {}
    var onFavoriteClick: OnFavoriteClickListener = { _, _ -> }
    var onShareClick: OnShareClickListener = {}

    init {
        binding.favorite.setOnClickListener { view -> onFavoriteClick(state.deviationId, (view as Checkable).isChecked) }
        binding.comments.setOnClickListener { onCommentsClick(state.commentThreadId!!) }
        binding.share.setOnClickListener { onShareClick(state.shareObjectId) }
    }

    fun render(state: DeviationActionsState) {
        this.state = state
        binding.favorite.text = state.favoriteCount.toString()
        binding.favorite.isChecked = state.isFavorite
        binding.comments.text = state.commentCount.toString()
        binding.comments.isVisible = state.commentThreadId != null
    }
}
