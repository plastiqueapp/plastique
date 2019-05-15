package io.plastique.core.breadcrumbs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Breadcrumb(
    val text: String,
    val tag: Parcelable
) : Parcelable
