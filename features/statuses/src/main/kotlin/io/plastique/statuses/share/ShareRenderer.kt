package io.plastique.statuses.share

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.statuses.R
import io.plastique.statuses.databinding.IncStatusesSharedDeviationImageBinding
import io.plastique.statuses.databinding.IncStatusesSharedDeviationLiteratureBinding
import io.plastique.statuses.databinding.IncStatusesSharedObjectDeletedBinding
import io.plastique.statuses.databinding.IncStatusesSharedStatusBinding
import io.plastique.util.dimensionRatio

internal interface ShareRenderer {
    fun render(share: ShareUiModel)
}

internal class NoneRenderer(private val parent: ViewGroup) : ShareRenderer {
    override fun render(share: ShareUiModel) {
        require(share === ShareUiModel.None)
        parent.setBackgroundResource(0)
    }
}

internal class ImageDeviationRenderer(private val parent: ViewGroup, private val imageLoader: ImageLoader) : ShareRenderer {
    private val binding = IncStatusesSharedDeviationImageBinding.inflate(parent.layoutInflater, parent, true)

    override fun render(share: ShareUiModel) {
        require(share is ShareUiModel.ImageDeviation)
        parent.setBackgroundResource(R.drawable.status_share_background)
        binding.header.setUser(share.author, imageLoader)
        binding.title.text = share.title
        binding.matureContent.isVisible = share.isConcealedMature
        (binding.preview.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = share.preview.size.dimensionRatio

        if (share.isConcealedMature) {
            imageLoader.cancel(binding.preview)
            binding.preview.setImageDrawable(null)
            binding.preview.setBackgroundResource(R.color.statuses_placeholder_background)
        } else {
            binding.preview.setBackgroundResource(0)

            imageLoader.load(share.preview.url)
                .params {
                    transforms += TransformType.CenterCrop
                    cacheSource = true
                }
                .into(binding.preview)
        }
    }
}

internal class LiteratureDeviationRenderer(
    private val parent: ViewGroup,
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter
) : ShareRenderer {
    private val binding = IncStatusesSharedDeviationLiteratureBinding.inflate(parent.layoutInflater, parent, true)

    override fun render(share: ShareUiModel) {
        require(share is ShareUiModel.LiteratureDeviation)
        parent.setBackgroundResource(R.drawable.status_share_background)
        binding.header.setUser(share.author, imageLoader)
        binding.header.time = if (share.isJournal) elapsedTimeFormatter.format(share.date) else null
        binding.title.text = share.title
        binding.excerpt.text = share.excerpt.value
    }
}

internal class StatusRenderer(
    private val parent: ViewGroup,
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter
) : ShareRenderer {
    private val binding = IncStatusesSharedStatusBinding.inflate(parent.layoutInflater, parent, true)

    override fun render(share: ShareUiModel) {
        require(share is ShareUiModel.Status)
        parent.setBackgroundResource(R.drawable.status_share_background)
        binding.header.setUser(share.author, imageLoader)
        binding.header.time = elapsedTimeFormatter.format(share.date)
        binding.statusText.text = share.text.value
    }
}

internal class DeletedRenderer(private val parent: ViewGroup) : ShareRenderer {
    private val binding = IncStatusesSharedObjectDeletedBinding.inflate(parent.layoutInflater, parent, true)

    override fun render(share: ShareUiModel) {
        parent.setBackgroundResource(R.drawable.status_share_deleted_background)
        when (share) {
            is ShareUiModel.DeletedDeviation ->
                binding.text.setText(R.string.statuses_shared_deviation_deleted)
            is ShareUiModel.DeletedStatus ->
                binding.text.setText(R.string.statuses_shared_status_deleted)
            else -> throw IllegalArgumentException("Invalid share type $share")
        }
    }
}
