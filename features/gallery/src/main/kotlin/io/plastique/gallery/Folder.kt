package io.plastique.gallery

data class Folder constructor(
    val id: String,
    val name: String,
    val size: Int,
    val thumbnailUrl: String?,
    val isDeletable: Boolean
) {
    companion object {
        const val FEATURED = "Featured"
    }
}
