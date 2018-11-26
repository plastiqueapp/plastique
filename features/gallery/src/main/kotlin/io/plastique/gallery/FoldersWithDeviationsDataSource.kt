package io.plastique.gallery

import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.core.text.RichTextFormatter
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationDataSource
import io.plastique.deviations.list.DeviationItem
import io.plastique.deviations.list.ImageDeviationItem
import io.plastique.deviations.list.LiteratureDeviationItem
import io.plastique.util.Optional
import io.plastique.util.toOptional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class FoldersWithDeviationsDataSource @Inject constructor(
    private val foldersDataSource: FoldersDataSource,
    private val deviationDataSource: DeviationDataSource,
    private val richTextFormatter: RichTextFormatter
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
                                    Optional.None
                                }
                            }
                            .distinctUntilChanged()
                            .switchMap { featuredFolder ->
                                when (featuredFolder) {
                                    is Optional.Some -> getDeviationItems(params, featuredFolder.value)
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

    private fun getDeviationItems(params: FolderLoadParams, featuredFolder: Folder): Observable<ItemsData> {
        val folderParams = GalleryDeviationParams(
                folderId = GalleryFolderId(id = featuredFolder.id, username = params.username),
                showMatureContent = params.matureContent)
        return deviationDataSource.getData(folderParams)
                .map { pagedData ->
                    ItemsData(items = createDeviationItems(featuredFolder, pagedData.value), hasMore = pagedData.hasMore)
                }
    }

    private fun createDeviationItems(folder: Folder, deviations: List<Deviation>): List<ListItem> {
        return if (deviations.isNotEmpty()) {
            var index = 0
            listOf(HeaderItem(folderId = folder.id, title = folder.name)) + deviations.map { deviation -> createDeviationItem(deviation, index++) }
        } else {
            emptyList()
        }
    }

    private fun createDeviationItem(deviation: Deviation, index: Int): DeviationItem = if (deviation.isLiterature) {
        LiteratureDeviationItem(deviation, index = index, excerpt = richTextFormatter.format(deviation.excerpt!!))
    } else {
        ImageDeviationItem(deviation, index = index)
    }
}
