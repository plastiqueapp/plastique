package io.plastique.deviations.viewer

import android.graphics.Matrix
import android.view.View
import android.view.ViewStub
import android.webkit.WebView
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.chrisbanes.photoview.PhotoView
import io.plastique.core.image.ImageLoader
import io.plastique.deviations.R

internal abstract class DeviationContentView(stub: ViewStub, @LayoutRes layoutId: Int) {
    protected val rootView: View = stub.apply { layoutResource = layoutId }.inflate()
    var onTapListener: (() -> Unit)? = null

    abstract fun render(content: DeviationContent)
}

internal fun createContentView(imageLoader: ImageLoader, stub: ViewStub, content: DeviationContent): DeviationContentView = when (content) {
    is DeviationContent.Image -> ImageContentView(imageLoader, stub)
    is DeviationContent.Literature -> LiteratureContentView(stub)
    is DeviationContent.Video -> VideoContentView(imageLoader, stub)
}

private class ImageContentView(
    private val imageLoader: ImageLoader,
    stub: ViewStub
) : DeviationContentView(stub, R.layout.item_deviation_viewer_image) {
    private val imageView: PhotoView = rootView.findViewById(R.id.deviation_image)

    init {
        imageView.setOnPhotoTapListener { _, _, _ -> onTapListener?.invoke() }
    }

    override fun render(content: DeviationContent) {
        require(content is DeviationContent.Image)

        imageLoader.load(content.url)
            .params {
                thumbnailUrls = content.thumbnailUrls
            }
            .into(imageView) { view, drawable ->
                if (drawable != null) {
                    // Preserve current transformation matrix in case full resolution image gets loaded after a thumbnail
                    val matrix = Matrix()
                    view.getSuppMatrix(matrix)
                    view.setImageDrawable(drawable)
                    view.setSuppMatrix(matrix)
                } else {
                    view.setImageDrawable(drawable)
                }
            }
    }
}

private class LiteratureContentView(stub: ViewStub) : DeviationContentView(stub, R.layout.item_deviation_viewer_literature) {
    private val webView: WebView = rootView.findViewById(R.id.deviation_content)

    override fun render(content: DeviationContent) {
        require(content is DeviationContent.Literature)
        webView.loadDataWithBaseURL(null, content.html, "text/html; charset=utf-8", null, null)
    }
}

private class VideoContentView(
    private val imageLoader: ImageLoader,
    stub: ViewStub
) : DeviationContentView(stub, R.layout.item_deviation_viewer_video) {
    private val imageView: ImageView = rootView.findViewById(R.id.deviation_preview)

    override fun render(content: DeviationContent) {
        require(content is DeviationContent.Video)
        imageLoader.load(content.previewUrl)
            .params {
                cacheInMemory = false
            }
            .into(imageView)
    }
}
