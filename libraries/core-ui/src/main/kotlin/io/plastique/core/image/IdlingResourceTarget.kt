package io.plastique.core.image

import android.graphics.drawable.Drawable
import androidx.test.espresso.idling.CountingIdlingResource
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

internal class IdlingResourceTarget<T>(
    private val delegate: Target<T>,
    idlingResource: CountingIdlingResource
) : Target<T> {

    private val idlingResourceWrapper = IdlingResourceWrapper(idlingResource)

    override fun onLoadStarted(placeholder: Drawable?) {
        delegate.onLoadStarted(placeholder)
        idlingResourceWrapper.isLoading = true
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        delegate.onLoadFailed(errorDrawable)
        idlingResourceWrapper.isLoading = false
    }

    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
        delegate.onResourceReady(resource, transition)
        idlingResourceWrapper.isLoading = false
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        delegate.onLoadCleared(placeholder)
        idlingResourceWrapper.isLoading = false
    }

    override fun getRequest(): Request? {
        return delegate.request
    }

    override fun setRequest(request: Request?) {
        delegate.request = request
    }

    override fun getSize(cb: SizeReadyCallback) {
        delegate.getSize(cb)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
        delegate.removeCallback(cb)
    }

    override fun onStart() {
        delegate.onStart()
    }

    override fun onStop() {
        delegate.onStop()
    }

    override fun onDestroy() {
        delegate.onDestroy()
    }

    private class IdlingResourceWrapper(private val idlingResource: CountingIdlingResource) {
        var isLoading: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    if (value) {
                        idlingResource.increment()
                    } else {
                        idlingResource.decrement()
                    }
                }
            }
    }
}
