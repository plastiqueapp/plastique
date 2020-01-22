package io.plastique.core.image

import android.view.View
import androidx.annotation.CheckResult
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.RequestManager

class ImageLoader private constructor(glideSupplier: () -> RequestManager) {
    val glide: GlideRequests by lazy(LazyThreadSafetyMode.NONE) { glideSupplier() as GlideRequests }

    @CheckResult
    fun load(url: String?): ImageRequest {
        return ImageRequest(glide, url?.toUri())
    }

    fun cancel(view: View) {
        glide.clear(view)
    }

    companion object {
        fun from(activity: FragmentActivity): ImageLoader = ImageLoader { GlideApp.with(activity) }
        fun from(fragment: Fragment): ImageLoader = ImageLoader { GlideApp.with(fragment) }
    }
}
