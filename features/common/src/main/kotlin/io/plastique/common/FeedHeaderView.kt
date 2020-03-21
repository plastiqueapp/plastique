package io.plastique.common

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.common.databinding.ViewFeedHeaderBinding
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.users.OnUserClickListener
import io.plastique.users.User

class FeedHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding = ViewFeedHeaderBinding.inflate(layoutInflater, this)
    private lateinit var user: User

    var onUserClick: OnUserClickListener = {}

    var time: String? = null
        set(value) {
            field = value
            binding.date.text = value
            binding.date.isVisible = value != null
        }

    init {
        val onUserClickListener = OnClickListener { onUserClick(user) }
        binding.avatar.setOnClickListener(onUserClickListener)
        binding.username.setOnClickListener(onUserClickListener)
    }

    fun setUser(user: User, imageLoader: ImageLoader) {
        this.user = user

        binding.avatar.contentDescription = resources.getString(R.string.common_avatar_description, user.name)
        binding.username.text = user.name
        imageLoader.load(user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(binding.avatar)
    }
}
