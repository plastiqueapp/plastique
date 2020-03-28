package io.plastique.gallery

import com.gojuno.koptional.None
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationDataSource
import io.plastique.deviations.list.DeviationItemFactory
import io.plastique.gallery.deviations.GalleryDeviationParams
import io.plastique.gallery.folders.Folder
import io.plastique.gallery.folders.FolderLoadParams
import io.plastique.gallery.folders.FoldersDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class FoldersWithDeviationsDataSource @Inject constructor(
    private val foldersDataSource: FoldersDataSource,
    private val deviationDataSource: DeviationDataSource,
    private val deviationItemFactory: DeviationItemFactory
) {
    @Volatile private var hasMoreFolders = false
    @Volatile private var hasMoreDeviations = false

    fun items(params: FolderLoadParams): Observable<ItemsData> {
        return foldersDataSource.getData(params)
            .publish { folders ->
                val folderItems = folders
                    .map { pagedData ->
                        ItemsData(items = createFolderItems(pagedData.value), hasMore = pagedData.hasMore)
                    }
                    .doOnNext { hasMoreFolders = it.hasMore }

                val deviationItems = folders
                    .map { pagedData ->
                        if (!pagedData.hasMore) {
                            pagedData.value.find { folder -> folder.name == Folder.FEATURED }.toOptional()
                        } else {
                            None
                        }
                    }
                    .distinctUntilChanged()
                    .switchMap { featuredFolder ->
                        when (featuredFolder) {
                            is Some -> getDeviationItems(params, featuredFolder.value)
                            else -> Observable.just(ItemsData(items = emptyList()))
                        }
                    }
                    .doOnNext { hasMoreDeviations = it.hasMore }

                Observable.combineLatest(folderItems, deviationItems) { folderData, deviationsData ->
                    ItemsData(
                        items = folderData.items + deviationsData.items,
                        hasMore = folderData.hasMore || deviationsData.hasMore)
                }
            }
    }

    fun loadMore(): Completable = Completable.defer {
        when {
            hasMoreFolders -> foldersDataSource.loadMore()
            hasMoreDeviations -> deviationDataSource.loadMore()
            else -> Completable.complete()
        }
    }

    fun refresh(): Completable {
        return Completable.concatArray(foldersDataSource.refresh(), deviationDataSource.refresh())
    }

    private fun createFolderItems(folders: List<Folder>): List<FolderItem> {
        return folders.mapIndexed { index, folder -> FolderItem(folder = folder, index = index) }
    }

    private fun getDeviationItems(params: FolderLoadParams, featuredFolder: Folder): Observable<ItemsData> {
        val folderParams = GalleryDeviationParams(
            folderId = featuredFolder.id,
            showMatureContent = params.matureContent)
        return deviationDataSource.getData(folderParams)
            .map { pagedData ->
                ItemsData(items = createDeviationItems(featuredFolder, pagedData.value), hasMore = pagedData.hasMore)
            }
    }

    private fun createDeviationItems(folder: Folder, deviations: List<Deviation>): List<ListItem> {
        return if (deviations.isNotEmpty()) {
            listOf(HeaderItem(folderId = folder.id.id, title = folder.name)) +
                    deviations.mapIndexed { index, deviation -> deviationItemFactory.create(deviation, index) }
        } else {
            emptyList()
        }
    }
}
