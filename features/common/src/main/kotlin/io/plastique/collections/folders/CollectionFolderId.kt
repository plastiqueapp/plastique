package io.plastique.collections.folders

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CollectionFolderId(
    @Json(name = "id")
    val id: String,

    @Json(name = "owner")
    val owner: String?
) : Parcelable
