package io.plastique.statuses

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.plastique.core.FeedHeaderView
import io.plastique.glide.GlideRequests
import io.plastique.util.dimensionRatio

class ShareView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private var layoutId: Int = 0
    private var share: ShareUiModel = ShareUiModel.None

    fun setShare(share: ShareUiModel, glide: GlideRequests) {
        if (this.share != share) {
            this.share = share
            renderShare(share, glide)
        }
    }

    private fun renderShare(share: ShareUiModel, glide: GlideRequests) {
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
                val matureContentView: TextView = findViewById(R.id.mature_content)
                headerView.setUser(share.author, glide)
                titleView.text = share.title
                matureContentView.isVisible = share.isConcealedMature
                (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = share.preview.size.dimensionRatio

                if (share.isConcealedMature) {
                    glide.clear(imageView)
                    imageView.setImageDrawable(null)
                    imageView.setBackgroundResource(R.color.statuses_placeholder_background)
                } else {
                    imageView.setBackgroundResource(0)

                    glide.load(share.preview.url)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }
            }

            is ShareUiModel.LiteratureDeviation -> {
                setLayout(R.layout.inc_feed_shared_deviation_literature)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val titleView: TextView = findViewById(R.id.deviation_title)
                val excerptView: TextView = findViewById(R.id.deviation_excerpt)
                headerView.setUser(share.author, glide)
                headerView.date = if (share.isJournal) share.date else null
                titleView.text = share.title
                excerptView.text = share.excerpt.value
            }

            is ShareUiModel.Status -> {
                setLayout(R.layout.inc_feed_shared_status)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val statusTextView: TextView = findViewById(R.id.status_text)
                headerView.setUser(share.author, glide)
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
