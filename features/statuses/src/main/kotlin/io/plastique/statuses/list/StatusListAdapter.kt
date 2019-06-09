package io.plastique.statuses.list

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.common.FeedHeaderView
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.core.text.RichTextView
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.glide.GlideRequests
import io.plastique.statuses.R
import io.plastique.statuses.ShareObjectId
import io.plastique.statuses.ShareUiModel
import io.plastique.statuses.ShareView
import io.plastique.statuses.isDeleted

class StatusItemDelegate(
    private val glide: GlideRequests,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<StatusItem, ListItem, StatusItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is StatusItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statuses_status, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: StatusItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.author, glide)
        holder.textView.text = item.statusText.value
        holder.commentsButton.text = item.commentCount.toString()
        holder.shareButton.isVisible = !item.share.isDeleted

        holder.shareView.setShare(item.share, glide, elapsedTimeFormatter)
        holder.shareView.setOnClickListener(if (!item.share.isDeleted) holder else null)
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val textView: RichTextView = itemView.findViewById(R.id.status_text)
        val shareView: ShareView = itemView.findViewById(R.id.status_share)
        val commentsButton: TextView = itemView.findViewById(R.id.button_comments)
        val shareButton: View = itemView.findViewById(R.id.button_share)

        init {
            textView.setOnClickListener(this)
            textView.movementMethod = LinkMovementMethod.getInstance()
            commentsButton.setOnClickListener(this)
            shareButton.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class StatusListAdapter(
    glide: GlideRequests,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onDeviationClick: OnDeviationClickListener,
    private val onStatusClick: OnStatusClickListener,
    private val onShareClick: OnShareClickListener,
    private val onCommentsClick: OnCommentsClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(StatusItemDelegate(glide, elapsedTimeFormatter, this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        val item = items[position] as StatusItem
        when (view.id) {
            R.id.button_comments -> onCommentsClick(item.statusId)
            R.id.button_share -> onShareClick(getObjectToShare(item))
            R.id.status_share -> {
                when (item.share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(item.share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(item.share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(item.share.statusId)
                    else -> throw IllegalStateException("Unexpected share $item.share")
                }
            }
        }
    }

    private fun getObjectToShare(item: StatusItem): ShareObjectId = when (item.share) {
        is ShareUiModel.None -> ShareObjectId.Status(item.statusId)
        is ShareUiModel.ImageDeviation -> ShareObjectId.Deviation(item.share.deviationId, item.statusId)
        is ShareUiModel.LiteratureDeviation -> ShareObjectId.Deviation(item.share.deviationId, item.statusId)
        is ShareUiModel.Status -> ShareObjectId.Status(item.share.statusId, item.statusId)
        else -> throw IllegalStateException("Nothing to share")
    }
}

typealias OnCommentsClickListener = (statusId: String) -> Unit
typealias OnDeviationClickListener = (deviationId: String) -> Unit
typealias OnShareClickListener = (shareObjectId: ShareObjectId) -> Unit
typealias OnStatusClickListener = (statusId: String) -> Unit
