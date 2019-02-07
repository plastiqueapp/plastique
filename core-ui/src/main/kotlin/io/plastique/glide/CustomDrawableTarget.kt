package io.plastique.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.util.Util

abstract class CustomDrawableTarget(
    private val width: Int = Target.SIZE_ORIGINAL,
    private val height: Int = Target.SIZE_ORIGINAL
) : Target<Drawable> {

    private var request: Request? = null

    init {
        if (!Util.isValidDimensions(width, height)) {
            throw IllegalArgumentException("Invalid dimensions: width=$width, height=$height")
        }
    }

    protected abstract fun setDrawable(resource: Drawable?)

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        setDrawable(resource)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        setDrawable(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        setDrawable(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        setDrawable(placeholder)
    }

    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(width, height)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
        // Do nothing
    }

    override fun onStart() {
        // Do nothing
    }

    override fun onStop() {
        // Do nothing
    }

    override fun onDestroy() {
        // Do nothing
    }

    override fun getRequest(): Request? = request

    override fun setRequest(request: Request?) {
        this.request = request
    }
}
