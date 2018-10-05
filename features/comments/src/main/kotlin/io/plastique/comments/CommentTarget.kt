package io.plastique.comments

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class CommentTarget : Parcelable {
    abstract val key: String

    @Parcelize
    data class Deviation(val deviationId: String) : CommentTarget() {
        override val key: String
            get() = "comments-deviation-$deviationId"
    }

    @Parcelize
    data class Profile(val username: String) : CommentTarget() {
        override val key: String
            get() = "comments-profile-$username"
    }

    @Parcelize
    data class Status(val statusId: String) : CommentTarget() {
        override val key: String
            get() = "comments-status-$statusId"
    }
}
