package io.plastique.statuses.list

import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import com.github.technoir42.android.extensions.layoutInflater
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.OnCommentsClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.statuses.OnShareClickListener
import io.plastique.statuses.OnStatusClickListener
import io.plastique.statuses.databinding.ItemStatusesStatusBinding
import io.plastique.statuses.share.ShareUiModel
import io.plastique.statuses.share.isDeleted

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
        val binding = ItemStatusesStatusBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onStatusClick, onDeviationClick, onCommentsClick, onShareClick)
    }

    override fun onBindViewHolder(item: StatusItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.author, imageLoader)
        holder.binding.text.text = item.statusText.value
        holder.binding.actions.render(item.actionsState)

        holder.binding.share.setShare(item.share, imageLoader, elapsedTimeFormatter)
        holder.binding.share.isClickable = !item.share.isDeleted
    }

    class ViewHolder(
        val binding: ItemStatusesStatusBinding,
        onStatusClick: OnStatusClickListener,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onShareClick: OnShareClickListener
    ) : BaseAdapterDelegate.ViewHolder<StatusItem>(binding.root) {

        init {
            binding.text.setOnClickListener { onStatusClick(item.statusId) }
            binding.text.movementMethod = LinkMovementMethod.getInstance()
            binding.share.setOnClickListener {
                when (val share = item.share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(share.statusId)
                }
            }
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onShareClick = onShareClick
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
