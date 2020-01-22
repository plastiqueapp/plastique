package io.plastique.core.image

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

internal class DrawableCallbackTarget(
    private val callback: (Drawable?) -> Unit
) : CustomTarget<Drawable>() {

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        callback(resource)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        callback(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        callback(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        callback(placeholder)
    }
}
