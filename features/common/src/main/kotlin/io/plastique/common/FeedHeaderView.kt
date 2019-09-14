package io.plastique.common

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.plastique.glide.GlideRequests
import io.plastique.users.User

class FeedHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val avatarView: ImageView
    private val usernameView: TextView
    private val dateView: TextView

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
    }

    fun setUser(user: User, glide: GlideRequests) {
        avatarView.contentDescription = resources.getString(R.string.common_avatar_description, user.name)
        usernameView.text = user.name
        glide.load(user.avatarUrl)
            .fallback(R.drawable.default_avatar_64dp)
            .circleCrop()
            .dontAnimate()
            .into(avatarView)
    }

    fun setOnUserClickListener(listener: OnClickListener) {
        val wrapper = OnClickListener { listener.onClick(this) }
        avatarView.setOnClickListener(wrapper)
        usernameView.setOnClickListener(wrapper)
    }
}
