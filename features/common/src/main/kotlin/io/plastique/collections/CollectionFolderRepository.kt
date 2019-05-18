package io.plastique.collections

import io.plastique.api.collections.FolderDto
import io.plastique.core.cache.CleanableRepository
import io.reactivex.Completable

interface CollectionFolderRepository : CleanableRepository {
    fun put(folders: Collection<FolderDto>)

    fun markAsDeleted(folderId: String, deleted: Boolean): Completable

    fun deleteMarkedFolders(): Completable
}
