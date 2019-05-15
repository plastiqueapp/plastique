package io.plastique.collections

import io.plastique.api.collections.FolderDto

interface CollectionFolderRepository {
    fun put(folders: Collection<FolderDto>)
}
