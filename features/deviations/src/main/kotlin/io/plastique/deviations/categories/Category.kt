package io.plastique.deviations.categories

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

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
) : Serializable {

    override fun toString(): String {
        return "Category(path=$path, title=$title, parent=${parent?.path}, hasChildren=$hasChildren)"
    }

    companion object {
        val ALL = Category("/", "All categories", null, true)
    }
}

fun CategoryEntity.toCategory(parentCategory: Category): Category {
    if (parentCategory.path != parent) {
        throw IllegalArgumentException("Expected Category with id $parent but got ${parentCategory.path}")
    }
    return Category(path = path, title = title, parent = parentCategory, hasChildren = hasChildren)
}
