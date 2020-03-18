package io.plastique.common

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.users.User

class FeedHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val avatarView: ImageView
    private val usernameView: TextView
    private val dateView: TextView
    private lateinit var user: User

    var onUserClickListener: OnUserClickListener? = null

    var time: String? = null
        set(value) {
            field = value
            dateView.text = value
            dateView.isVisible = value != null
        }

    init {
        inflate(context, R.layout.view_feed_header, this)
        avatarView = findViewById(R.id.avatar)
        usernameView = findViewById(R.id.username)
        dateView = findViewById(R.id.date)

        val onClickListener = OnClickListener { onUserClickListener?.invoke(user) }
        avatarView.setOnClickListener(onClickListener)
        usernameView.setOnClickListener(onClickListener)
    }

    fun setUser(user: User, imageLoader: ImageLoader) {
        this.user = user

        avatarView.contentDescription = resources.getString(R.string.common_avatar_description, user.name)
        usernameView.text = user.name
        imageLoader.load(user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(avatarView)
    }
}

typealias OnUserClickListener = (User) -> Unit
