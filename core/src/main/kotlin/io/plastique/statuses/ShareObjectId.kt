package io.plastique.statuses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ShareObjectId : Parcelable {
    @Parcelize
    data class Deviation(val deviationId: String, val fromStatusId: String? = null) : ShareObjectId()

    @Parcelize
    data class Status(val statusId: String, val fromStatusId: String? = null) : ShareObjectId()
}
