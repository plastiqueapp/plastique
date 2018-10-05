package io.plastique.collections

import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationDataSource
import io.plastique.deviations.list.DeviationItem
import io.plastique.util.Optional
import io.plastique.util.toOptional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class FoldersWithDeviationsDataSource @Inject constructor(
    private val foldersDataSource: FoldersDataSource,
    private val deviationDataSource: DeviationDataSource
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
                            .filter { pagedData -> !pagedData.hasMore }
                            .map { pagedData -> pagedData.value.find { folder -> folder.name == FEATURED_FOLDER_NAME }.toOptional() }
                            .distinctUntilChanged()
                            .switchMap { featuredFolder ->
                                when (featuredFolder) {
                                    is Optional.Some -> {
                                        val folderParams = CollectionDeviationParams(
                                                folderId = featuredFolder.value.id,
                                                username = params.username,
                                                showMatureContent = params.matureContent)
                                        deviationDataSource.getData(folderParams)
                                                .map { pagedData ->
                                                    ItemsData(items = createDeviationItems(featuredFolder.value, pagedData.value), hasMore = pagedData.hasMore)
                                                }
                                    }
                                    else -> Observable.just(ItemsData(items = emptyList()))
                                }
                            }
                            .doOnNext { hasMoreDeviations = it.hasMore }

                    Observables.combineLatest(folderItems, deviationItems) { folderData, deviationsData ->
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
        var index = 0
        return folders.map { folder -> FolderItem(folder).also { it.index = index++ } }
    }

    private fun createDeviationItems(folder: Folder, deviations: List<Deviation>): List<ListItem> {
        return if (deviations.isNotEmpty()) {
            var index = 0
            listOf(HeaderItem(folderId = folder.id, title = folder.name)) + deviations.map { deviation -> DeviationItem(deviation).also { it.index = index++ } }
        } else {
            emptyList()
        }
    }

    private companion object {
        private const val FEATURED_FOLDER_NAME = "Featured"
    }
}
