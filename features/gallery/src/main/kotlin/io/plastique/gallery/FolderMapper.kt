package io.plastique.gallery

import javax.inject.Inject

class FolderMapper @Inject constructor() {
    fun map(folder: FolderEntity): Folder {
        return Folder(
                id = folder.id,
                name = folder.name,
                size = folder.size,
                thumbnailUrl = folder.thumbnailUrl)
    }
}
