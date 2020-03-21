package io.plastique.statuses.share

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.plastique.core.image.ImageLoader
import io.plastique.core.time.ElapsedTimeFormatter

class ShareView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private var share: ShareUiModel = ShareUiModel.None
    private var renderer: ShareRenderer = NoneRenderer(this)

    fun setShare(share: ShareUiModel, imageLoader: ImageLoader, elapsedTimeFormatter: ElapsedTimeFormatter) {
        if (this.share.javaClass != share.javaClass) {
            removeAllViews()
            renderer = createRenderer(share, imageLoader, elapsedTimeFormatter)
        }
        if (this.share != share) {
            this.share = share
            renderer.render(share)
        }
    }

    private fun createRenderer(share: ShareUiModel, imageLoader: ImageLoader, elapsedTimeFormatter: ElapsedTimeFormatter): ShareRenderer {
        return when (share) {
            ShareUiModel.None -> NoneRenderer(this)
            is ShareUiModel.ImageDeviation -> ImageDeviationRenderer(this, imageLoader)
            is ShareUiModel.LiteratureDeviation -> LiteratureDeviationRenderer(this, imageLoader, elapsedTimeFormatter)
            is ShareUiModel.Status -> StatusRenderer(this, imageLoader, elapsedTimeFormatter)
            ShareUiModel.DeletedDeviation,
            ShareUiModel.DeletedStatus -> DeletedRenderer(this)
        }
    }
}
