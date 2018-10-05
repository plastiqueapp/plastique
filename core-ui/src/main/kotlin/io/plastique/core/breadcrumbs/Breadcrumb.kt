package io.plastique.core.breadcrumbs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Breadcrumb(
    val text: String,
    val tag: Serializable
) : Parcelable
