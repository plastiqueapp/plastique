package io.plastique.core

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.plastique.core.ui.R
import io.plastique.glide.GlideRequests
import io.plastique.users.User
import io.plastique.util.ElapsedTimeFormatter
import org.threeten.bp.ZonedDateTime

class FeedHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val avatarView: ImageView
    private val usernameView: TextView
    private val dateView: TextView

    var date: ZonedDateTime? = null
        set(value) {
            if (field != value) {
                field = value
                renderDate(value)
            }
        }

    init {
        inflate(context, R.layout.view_feed_header, this)
        avatarView = findViewById(R.id.avatar)
        usernameView = findViewById(R.id.username)
        dateView = findViewById(R.id.date)
    }

    fun setUser(user: User, glide: GlideRequests) {
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

    private fun renderDate(dateTime: ZonedDateTime?) {
        dateView.text = if (dateTime != null) ElapsedTimeFormatter.format(context, dateTime, ZonedDateTime.now()) else null
    }
}
