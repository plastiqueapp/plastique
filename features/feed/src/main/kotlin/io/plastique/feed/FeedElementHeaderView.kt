package io.plastique.feed

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.plastique.glide.GlideApp
import io.plastique.users.User
import io.plastique.util.ElapsedTimeFormatter
import org.threeten.bp.ZonedDateTime

class FeedElementHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    private val avatarView: ImageView
    private val usernameView: TextView
    private val dateView: TextView

    var user: User? = null
        set(value) {
            requireNotNull(value)
            if (field != value) {
                field = value
                renderUser(value)
            }
        }

    var date: ZonedDateTime? = null
        set(value) {
            requireNotNull(value)
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

    fun setOnUserClickListener(listener: View.OnClickListener) {
        val wrapper = View.OnClickListener { listener.onClick(this) }
        avatarView.setOnClickListener(wrapper)
        usernameView.setOnClickListener(wrapper)
    }

    private fun renderUser(user: User) {
        usernameView.text = user.name

        GlideApp.with(avatarView)
                .load(user.avatarUrl)
                .fallback(R.drawable.default_avatar_64dp)
                .circleCrop()
                .dontAnimate()
                .into(avatarView)
    }

    private fun renderDate(dateTime: ZonedDateTime) {
        dateView.text = ElapsedTimeFormatter.format(context, dateTime, ZonedDateTime.now())
    }
}
