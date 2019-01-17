package io.plastique.deviations.categories

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
    val path: String,
    val title: String,
    val parent: Category?,
    val hasChildren: Boolean
) : Parcelable {

    override fun toString(): String {
        return "Category(path=$path, title=$title, parent=${parent?.path}, hasChildren=$hasChildren)"
    }

    companion object {
        val ALL = Category(path = "/", title = "All categories", parent = null, hasChildren = true)
    }
}
