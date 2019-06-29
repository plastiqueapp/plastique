package io.plastique.deviations.viewer

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewStub
import android.webkit.WebView
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.bumptech.glide.request.target.ImageViewTarget
import com.github.chrisbanes.photoview.PhotoView
import io.plastique.deviations.R
import io.plastique.glide.GlideRequest
import io.plastique.glide.GlideRequests

internal abstract class DeviationContentView(stub: ViewStub, @LayoutRes layoutId: Int) {
    protected val rootView: View = stub.apply { layoutResource = layoutId }.inflate()
    var onTapListener: (() -> Unit)? = null

    abstract fun render(content: DeviationContent)
}

internal fun createContentView(glide: GlideRequests, stub: ViewStub, content: DeviationContent): DeviationContentView = when (content) {
    is DeviationContent.Image -> ImageContentView(glide, stub)
    is DeviationContent.Literature -> LiteratureContentView(stub)
    is DeviationContent.Video -> VideoContentView(glide, stub)
}

private class ImageContentView(
    private val glide: GlideRequests,
    stub: ViewStub
) : DeviationContentView(stub, R.layout.item_deviation_viewer_image) {
    private val imageView: PhotoView = rootView.findViewById(R.id.deviation_image)

    init {
        imageView.setOnPhotoTapListener { _, _, _ -> onTapListener?.invoke() }
    }

    override fun render(content: DeviationContent) {
        require(content is DeviationContent.Image)

        val thumbnailRequest = content.thumbnailUrls.asSequence()
            .fold<String, GlideRequest<Drawable>?>(null) { previous, thumbnailUrl ->
                val current = glide.load(thumbnailUrl).onlyRetrieveFromCache(true)
                if (previous != null) {
                    current.thumbnail(previous)
                } else {
                    current
                }
            }

        glide.load(content.url)
            .thumbnail(thumbnailRequest)
            .into<ImageViewTarget<Drawable>>(object : ImageViewTarget<Drawable>(imageView) {
                override fun setResource(resource: Drawable?) {
                    if (resource != null) {
                        require(view is PhotoView)

                        // Preserve current transformation matrix in case full resolution image was loaded after a thumbnail
                        val matrix = Matrix()
                        view.getSuppMatrix(matrix)
                        view.setImageDrawable(resource)
                        view.setSuppMatrix(matrix)
                    } else {
                        view.setImageDrawable(resource)
                    }
                }
            })
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
    private val glide: GlideRequests,
    stub: ViewStub
) : DeviationContentView(stub, R.layout.item_deviation_viewer_video) {
    private val imageView: ImageView = rootView.findViewById(R.id.deviation_preview)

    override fun render(content: DeviationContent) {
        require(content is DeviationContent.Video)
        glide.load(content.previewUrl)
            .skipMemoryCache(true)
            .into(imageView)
    }
}
