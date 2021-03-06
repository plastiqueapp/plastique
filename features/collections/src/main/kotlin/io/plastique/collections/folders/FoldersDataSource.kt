package io.plastique.collections.folders

import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.PagedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class FoldersDataSource @Inject constructor(
    private val folderRepository: CollectionFolderRepositoryImpl
) : PagedDataSource<List<Folder>, FolderLoadParams> {
    private val params = AtomicReference<FolderLoadParams>()
    private val nextCursor = AtomicReference<OffsetCursor>()

    override fun getData(params: FolderLoadParams): Observable<out PagedData<List<Folder>, *>> {
        this.params.set(params)
        return folderRepository.getFolders(params)
            .doOnNext { data -> nextCursor.set(data.nextCursor) }
    }

    override fun loadMore(): Completable {
        return folderRepository.fetchFolders(params.get(), nextCursor.get()!!)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    override fun refresh(): Completable {
        return folderRepository.fetchFolders(params.get())
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }
}
