package io.plastique.feed

import io.plastique.deviations.FetchParams
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BucketDeviationParams(
    val bucketId: String,

    override val showMatureContent: Boolean,
    override val showLiterature: Boolean
) : FetchParams {

    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
    }
}
