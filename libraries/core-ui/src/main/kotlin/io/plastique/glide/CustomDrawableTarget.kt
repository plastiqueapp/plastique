package io.plastique.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

abstract class CustomDrawableTarget(
    width: Int = Target.SIZE_ORIGINAL,
    height: Int = Target.SIZE_ORIGINAL
) : CustomTarget<Drawable>(width, height) {

    protected abstract fun setDrawable(drawable: Drawable?)

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
}
