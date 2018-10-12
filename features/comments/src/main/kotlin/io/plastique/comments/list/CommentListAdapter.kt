package io.plastique.comments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.R
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.glide.GlideApp
import io.plastique.users.User
import io.plastique.util.ElapsedTimeFormatter
import org.threeten.bp.ZonedDateTime

class CommentItemDelegate(
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<CommentItem, ListItem, CommentItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is CommentItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: CommentItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.authorView.text = item.comment.author.name
        holder.textView.text = item.comment.text
        holder.timeView.text = ElapsedTimeFormatter.format(holder.timeView.context, item.comment.datePosted, ZonedDateTime.now())

        val replyingTo = if (item.comment.isReply) {
            holder.itemView.context.getString(R.string.comments_replying_to, item.comment.parentAuthorName)
        } else {
            null
        }

        holder.replyingToView.text = replyingTo
        holder.replyingToView.isVisible = replyingTo != null
        holder.replyButton.isVisible = item.showReplyButton

        GlideApp.with(holder.avatarView)
                .asBitmap()
                .load(item.comment.author.avatarUrl)
                .circleCrop()
                .into(holder.avatarView)
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val avatarView: ImageView = itemView.findViewById(R.id.comment_author_avatar)
        val authorView: TextView = itemView.findViewById(R.id.comment_author_name)
        val timeView: TextView = itemView.findViewById(R.id.comment_time)
        val replyingToView: TextView = itemView.findViewById(R.id.comment_replying_to)
        val textView: TextView = itemView.findViewById(R.id.comment_text)
        val replyButton: ImageButton = itemView.findViewById(R.id.comment_button_reply)

        init {
            itemView.setOnClickListener(this)
            authorView.setOnClickListener(this)
            avatarView.setOnClickListener(this)
            replyButton.setOnClickListener(this)
            replyingToView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class CommentsAdapter : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {
    var onOpenUserProfileListener: OnOpenUserProfileListener? = null
    var onReplyClickListener: OnReplyClickListener? = null
    var onReplyingToClickListener: OnReplyingToClickListener? = null

    init {
        delegatesManager.addDelegate(CommentItemDelegate(this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        val item = items[position] as CommentItem
        if (holder is CommentItemDelegate.ViewHolder) {
            if (view === holder.avatarView || view === holder.authorView) {
                onOpenUserProfileListener?.invoke(item.comment.author)
            } else if (view === holder.replyButton) {
                onReplyClickListener?.invoke(item.comment.id)
            } else if (view === holder.replyingToView) {
                onReplyingToClickListener?.invoke(item.comment.id)
            }
        }
    }

    fun findCommentPosition(commentId: String): Int {
        for ((index, item) in items.withIndex()) {
            if (item is CommentItem) {
                if (item.comment.id == commentId) {
                    return index
                }
            }
        }
        return RecyclerView.NO_POSITION
    }
}

typealias OnOpenUserProfileListener = (user: User) -> Unit
typealias OnReplyClickListener = (commentId: String) -> Unit
typealias OnReplyingToClickListener = (commentId: String) -> Unit