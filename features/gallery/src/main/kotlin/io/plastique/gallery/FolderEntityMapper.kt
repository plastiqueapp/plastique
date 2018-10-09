package io.plastique.gallery

import io.plastique.api.gallery.Folder
import javax.inject.Inject

class FolderEntityMapper @Inject constructor() {
    fun map(folder: Folder): FolderEntity {
        return FolderEntity(
                id = folder.id,
                name = folder.name,
                size = folder.size,
                thumbnailUrl = getThumbnailUrl(folder))
    }

    private fun getThumbnailUrl(folder: Folder): String? {
        return folder.deviations.asSequence()
                .map { deviation -> deviation.preview?.url ?: deviation.content?.url }
                .firstOrNull()
    }
}
