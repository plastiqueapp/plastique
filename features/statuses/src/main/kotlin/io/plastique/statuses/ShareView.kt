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
import io.plastique.common.FeedHeaderView
import io.plastique.core.text.RichTextView
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.glide.GlideRequests
import io.plastique.util.dimensionRatio

class ShareView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private var layoutId: Int = 0
    private var share: ShareUiModel = ShareUiModel.None

    fun setShare(share: ShareUiModel, glide: GlideRequests, elapsedTimeFormatter: ElapsedTimeFormatter) {
        if (this.share != share) {
            this.share = share
            renderShare(share, glide, elapsedTimeFormatter)
        }
    }

    private fun renderShare(share: ShareUiModel, glide: GlideRequests, elapsedTimeFormatter: ElapsedTimeFormatter) {
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
                val previewView: ImageView = findViewById(R.id.deviation_preview)
                val matureContentView: TextView = findViewById(R.id.mature_content)
                headerView.setUser(share.author, glide)
                titleView.text = share.title
                matureContentView.isVisible = share.isConcealedMature
                (previewView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = share.preview.size.dimensionRatio

                if (share.isConcealedMature) {
                    glide.clear(previewView)
                    previewView.setImageDrawable(null)
                    previewView.setBackgroundResource(R.color.statuses_placeholder_background)
                } else {
                    previewView.setBackgroundResource(0)

                    glide.load(share.preview.url)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(previewView)
                }
            }

            is ShareUiModel.LiteratureDeviation -> {
                setLayout(R.layout.inc_feed_shared_deviation_literature)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val titleView: TextView = findViewById(R.id.deviation_title)
                val excerptView: RichTextView = findViewById(R.id.deviation_excerpt)
                headerView.setUser(share.author, glide)
                headerView.time = if (share.isJournal) elapsedTimeFormatter.format(share.date) else null
                titleView.text = share.title
                excerptView.text = share.excerpt.value
            }

            is ShareUiModel.Status -> {
                setLayout(R.layout.inc_feed_shared_status)
                setBackgroundResource(R.drawable.status_share_background)

                val headerView: FeedHeaderView = findViewById(R.id.header)
                val statusTextView: RichTextView = findViewById(R.id.status_text)
                headerView.setUser(share.author, glide)
                headerView.time = elapsedTimeFormatter.format(share.date)
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
