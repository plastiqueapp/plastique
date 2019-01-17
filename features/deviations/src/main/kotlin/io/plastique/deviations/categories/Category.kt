package io.plastique.deviations.categories

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Category(
    @Json(name = "path")
    val path: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "parent")
    val parent: Category? = null,

    @Json(name = "has_children")
    val hasChildren: Boolean = false
) : Parcelable {

    override fun toString(): String {
        return "Category(path=$path, title=$title, parent=${parent?.path}, hasChildren=$hasChildren)"
    }

    companion object {
        val ALL = Category(path = "/", title = "All categories", parent = null, hasChildren = true)
    }
}
