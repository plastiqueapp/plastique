package io.plastique.util

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Size(val width: Int, val height: Int) : Serializable, Parcelable {
    override fun toString(): String = width.toString() + "x" + height
}
