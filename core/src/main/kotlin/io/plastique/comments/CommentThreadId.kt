package io.plastique.comments

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class CommentThreadId : Parcelable {
    @Parcelize
    data class Deviation(val deviationId: String) : CommentThreadId()

    @Parcelize
    data class Profile(val username: String) : CommentThreadId()

    @Parcelize
    data class Status(val statusId: String) : CommentThreadId()
}
