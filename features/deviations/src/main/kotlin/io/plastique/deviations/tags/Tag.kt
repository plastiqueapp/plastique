package io.plastique.deviations.tags

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Tag(
    val type: Int,
    val text: String,
    val value: Serializable
) : Parcelable {

    companion object {
        const val TYPE_CATEGORY = 0
        const val TYPE_TIME_RANGE = 1
    }
}
