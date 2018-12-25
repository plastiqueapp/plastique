package io.plastique.statuses

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.plastique.core.FeedHeaderView
import io.plastique.glide.GlideApp
import io.plastique.util.dimensionRatio

class ShareView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private var layoutId: Int = 0
    var share: ShareUiModel = ShareUiModel.None
        set(value) {
            if (field != value) {
                field = value
                renderShare(value)
            }
        }

    private fun renderShare(share: ShareUiModel) {
        when (share) {
            ShareUiModel.None -> {
                setLayout(0)
                setBackgroundResource(0)
            }

            is ShareUiModel.ImageDeviation -> {
                setLayout(R.layout.inc_feed_shared_deviation_image)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val titleView: TextView = findViewById(R.id.deviation_title)
                val imageView: ImageView = findViewById(R.id.deviation_image)
                headerView.user = share.author
                titleView.text = share.title
                (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = share.preview.size.dimensionRatio

                GlideApp.with(imageView)
                        .load(share.preview.url)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
            }

            is ShareUiModel.LiteratureDeviation -> {
                setLayout(R.layout.inc_feed_shared_deviation_literature)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val titleView: TextView = findViewById(R.id.deviation_title)
                val excerptView: TextView = findViewById(R.id.deviation_excerpt)
                headerView.user = share.author
                headerView.date = if (share.isJournal) share.date else null
                titleView.text = share.title
                excerptView.text = share.excerpt.value
            }

            is ShareUiModel.Status -> {
                setLayout(R.layout.inc_feed_shared_status)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val statusTextView: TextView = findViewById(R.id.status_text)
                headerView.user = share.author
                headerView.date = share.date
                statusTextView.text = share.text.value
            }

            ShareUiModel.DeletedDeviation -> {
                setLayout(R.layout.inc_feed_shared_object_deleted)
                setBackgroundResource(R.drawable.status_share_deleted_background)

                val textView: TextView = findViewById(R.id.text)
                textView.setText(R.string.statuses_shared_deviation_deleted)
            }

            ShareUiModel.DeletedStatus -> {
                setLayout(R.layout.inc_feed_shared_object_deleted)
                setBackgroundResource(R.drawable.status_share_deleted_background)

                val textView: TextView = findViewById(R.id.text)
                textView.setText(R.string.statuses_shared_status_deleted)
            }
        }
    }

    private fun setLayout(layoutId: Int) {
        if (this.layoutId != layoutId) {
            this.layoutId = layoutId
            removeAllViews()

            if (layoutId != 0) {
                View.inflate(context, layoutId, this)
            }
        }
    }
}
