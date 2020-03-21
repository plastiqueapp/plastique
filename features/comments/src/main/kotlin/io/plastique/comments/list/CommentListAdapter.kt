package io.plastique.comments.list

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.layoutInflater
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.R
import io.plastique.comments.databinding.ItemCommentBinding
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.users.OnUserClickListener

private class CommentItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onReplyClick: OnReplyClickListener,
    private val onReplyingToClick: OnReplyingToClickListener,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<CommentItem, ListItem, CommentItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is CommentItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemCommentBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onReplyClick, onReplyingToClick, onUserClick)
    }

    override fun onBindViewHolder(item: CommentItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.binding.root.resources
        holder.binding.authorAvatar.contentDescription = resources.getString(R.string.common_avatar_description, item.comment.author.name)
        holder.binding.authorName.text = item.comment.author.name
        holder.binding.text.text = item.comment.text.value
        holder.binding.posted.text = elapsedTimeFormatter.format(item.comment.datePosted)

        val replyingTo = if (item.comment.isReply) {
            resources.getString(R.string.comments_replying_to, item.comment.parentAuthorName)
        } else {
            null
        }

        holder.binding.replyingTo.text = replyingTo
        holder.binding.replyingTo.isVisible = replyingTo != null
        holder.binding.reply.isVisible = item.showReplyButton

        imageLoader.load(item.comment.author.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.binding.authorAvatar)
    }

    class ViewHolder(
        val binding: ItemCommentBinding,
        onReplyClick: OnReplyClickListener,
        onReplyingToClick: OnReplyingToClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<CommentItem>(binding.root) {

        init {
            val onUserClickListener = View.OnClickListener { onUserClick(item.comment.author) }
            binding.authorName.setOnClickListener(onUserClickListener)
            binding.authorAvatar.setOnClickListener(onUserClickListener)
            binding.reply.setOnClickListener { onReplyClick(item.comment.id) }
            binding.replyingTo.setOnClickListener { onReplyingToClick(item.comment.id) }
        }
    }
}

internal class CommentListAdapter(
    imageLoader: ImageLoader,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    onReplyClick: OnReplyClickListener,
    onReplyingToClick: OnReplyingToClickListener,
    onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(CommentItemDelegate(imageLoader, elapsedTimeFormatter, onReplyClick, onReplyingToClick, onUserClick))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    fun findCommentPosition(commentId: String): Int {
        for ((index, item) in items.withIndex()) {
            if (item is CommentItem && item.comment.id == commentId) {
                return index
            }
        }
        return RecyclerView.NO_POSITION
    }
}

private typealias OnReplyClickListener = (commentId: String) -> Unit
private typealias OnReplyingToClickListener = (commentId: String) -> Unit
