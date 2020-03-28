package io.plastique.statuses.list

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import com.github.technoir42.android.extensions.inflate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.OnCommentsClickListener
import io.plastique.common.FeedHeaderView
import io.plastique.core.image.ImageLoader
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.text.RichTextView
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.statuses.OnShareClickListener
import io.plastique.statuses.OnStatusClickListener
import io.plastique.statuses.R
import io.plastique.statuses.ShareUiModel
import io.plastique.statuses.ShareView
import io.plastique.statuses.StatusActionsView
import io.plastique.statuses.isDeleted

private class StatusItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onStatusClick: OnStatusClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onShareClick: OnShareClickListener
) : BaseAdapterDelegate<StatusItem, ListItem, StatusItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is StatusItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_statuses_status)
        return ViewHolder(view, onStatusClick, onDeviationClick, onCommentsClick, onShareClick)
    }

    override fun onBindViewHolder(item: StatusItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.author, imageLoader)
        holder.textView.text = item.statusText.value
        holder.actionsView.render(item.actionsState)

        holder.shareView.setShare(item.share, imageLoader, elapsedTimeFormatter)
        holder.shareView.isClickable = !item.share.isDeleted
    }

    class ViewHolder(
        itemView: View,
        onStatusClick: OnStatusClickListener,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onShareClick: OnShareClickListener
    ) : BaseAdapterDelegate.ViewHolder<StatusItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val textView: RichTextView = itemView.findViewById(R.id.status_text)
        val shareView: ShareView = itemView.findViewById(R.id.status_share)
        val actionsView: StatusActionsView = itemView.findViewById(R.id.status_actions)

        init {
            textView.setOnClickListener { onStatusClick(item.statusId) }
            shareView.setOnClickListener {
                when (val share = item.share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(share.statusId)
                }
            }
            textView.movementMethod = LinkMovementMethod.getInstance()
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onShareClick = onShareClick
        }
    }
}

internal class StatusListAdapter(
    imageLoader: ImageLoader,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    onDeviationClick: OnDeviationClickListener,
    onStatusClick: OnStatusClickListener,
    onCommentsClick: OnCommentsClickListener,
    onShareClick: OnShareClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(StatusItemDelegate(imageLoader, elapsedTimeFormatter, onStatusClick, onDeviationClick, onCommentsClick, onShareClick))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }
}
