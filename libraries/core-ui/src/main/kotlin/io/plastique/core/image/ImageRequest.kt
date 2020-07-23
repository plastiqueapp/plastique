package io.plastique.core.image

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.test.espresso.idling.CountingIdlingResource
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.ImageViewTarget

class ImageRequest internal constructor(
    private val glide: GlideRequests,
    private val idlingResource: CountingIdlingResource,
    private val uri: Uri?,
    private val params: ImageLoadParams = ImageLoadParams()
) {
    fun params(builder: ImageLoadParams.() -> Unit): ImageRequest {
        params.builder()
        return this
    }

    fun <V : ImageView> into(view: V, callback: (V, Drawable?) -> Unit = DEFAULT_CALLBACK) {
        val target = object : ImageViewTarget<Drawable>(view) {
            override fun setResource(resource: Drawable?) {
                callback(view, resource)
            }
        }
        glide.load(uri)
            .applyParams(params)
            .applyScaleType(view.scaleType)
            .into(IdlingResourceTarget(target, idlingResource))
    }

    fun createPreloadRequest(): RequestBuilder<Drawable> {
        return glide.load(uri)
            .applyParams(params)
            .priority(Priority.LOW)
    }

    fun enqueue(callback: (Drawable?) -> Unit) {
        glide.load(uri)
            .applyParams(params)
            .into(IdlingResourceTarget(DrawableCallbackTarget(callback), idlingResource))
    }

    private fun GlideRequest<Drawable>.applyParams(params: ImageLoadParams): GlideRequest<Drawable> {
        var request = this
        if (params.errorDrawable != 0) {
            request = request.error(params.errorDrawable)
        }
        if (params.fallbackDrawable != 0) {
            request = request.fallback(params.fallbackDrawable)
        }
        if (params.placeholderDrawable != 0) {
            request = request.placeholder(params.placeholderDrawable)
        }
        params.thumbnailUrls?.let {
            request = request.thumbnail(createThumbnailRequest(it))
        }
        if (!params.animate) {
            request = request.dontAnimate()
        }
        params.size?.let { size ->
            request = request.override(size.width, size.height)
        }
        if (params.cacheSource) {
            request = request.diskCacheStrategy(DiskCacheStrategy.ALL)
        }
        if (!params.cacheInMemory) {
            request = request.skipMemoryCache(true)
        }
        for (transformType in params.transforms) {
            request = when (transformType) {
                TransformType.CenterCrop -> request.centerCrop()
                TransformType.CircleCrop -> request.circleCrop()
            }
        }
        return request
    }

    private fun GlideRequest<Drawable>.applyScaleType(scaleType: ScaleType): GlideRequest<Drawable> {
        if (!isTransformationSet && isTransformationAllowed) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (scaleType) {
                ScaleType.CENTER_CROP -> return optionalCenterCrop()
                ScaleType.CENTER_INSIDE -> return optionalCenterInside()
                ScaleType.FIT_CENTER,
                ScaleType.FIT_START,
                ScaleType.FIT_END -> return optionalFitCenter()
                ScaleType.FIT_XY -> return optionalCenterInside()
            }
        }
        return this
    }

    private fun createThumbnailRequest(thumbnailUrls: List<String>): GlideRequest<Drawable> {
        return thumbnailUrls.fold<String, GlideRequest<Drawable>?>(null) { previous, url ->
            val current = glide.load(url).onlyRetrieveFromCache(true)
            if (previous != null) {
                current.thumbnail(previous)
            } else {
                current
            }
        } ?: throw IllegalArgumentException("No thumbnail URLs")
    }

    companion object {
        private val DEFAULT_CALLBACK = { view: ImageView, drawable: Drawable? -> view.setImageDrawable(drawable) }
    }
}
