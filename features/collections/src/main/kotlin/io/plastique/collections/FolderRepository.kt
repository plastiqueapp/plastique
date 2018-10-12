package io.plastique.collections

import androidx.room.RoomDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.collections.CollectionService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.session.SessionManager
import io.plastique.core.session.currentUsername
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Duration
import javax.inject.Inject
import io.plastique.api.collections.Folder as FolderDto
import io.plastique.api.deviations.Deviation as DeviationDto

class FolderRepository @Inject constructor(
    private val database: RoomDatabase,
    private val collectionDao: CollectionDao,
    private val collectionService: CollectionService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val folderMapper: FolderMapper,
    private val folderEntityMapper: FolderEntityMapper,
    private val metadataConverter: NullFallbackConverter,
    private val sessionManager: SessionManager,
    private val timeProvider: TimeProvider
) {

    fun getFolders(params: FolderLoadParams): Observable<PagedData<List<Folder>, OffsetCursor>> {
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(timeProvider, CACHE_DURATION) { serializedMetadata ->
            val metadata = metadataConverter.fromJson<FolderCacheMetadata>(serializedMetadata)
            metadata?.params == params
        }
        val cacheHelper = CacheHelper(cacheEntryRepository, cacheEntryChecker)
        return Observable.defer {
            val cacheKey = getCacheKey(params.username ?: sessionManager.currentUsername)
            cacheHelper.createObservable(
                    cacheKey = cacheKey,
                    cachedData = getFoldersFromDb(cacheKey),
                    updater = fetchFolders(params, null, cacheKey))
        }
    }

    private fun getFoldersFromDb(cacheKey: String): Observable<PagedData<List<Folder>, OffsetCursor>> {
        return RxRoom.createObservable(database, arrayOf("collection_folders", "user_collection_folders")) {
            val folders = collectionDao.getFoldersByKey(cacheKey).map { folderMapper.map(it) }
            val nextCursor = getNextCursor(cacheKey)
            PagedData(folders, nextCursor)
        }
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<FolderCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor? = null): Completable {
        return Completable.defer {
            val cacheKey = getCacheKey(params.username ?: sessionManager.currentUsername)
            fetchFolders(params, cursor, cacheKey)
        }
    }

    private fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor?, cacheKey: String): Completable {
        val offset = cursor?.offset ?: 0
        return collectionService.getFolders(
                username = params.username,
                matureContent = params.matureContent,
                preload = true,
                offset = offset,
                limit = FOLDERS_PER_PAGE)
                .map { folderList ->
                    val nextCursor = if (folderList.hasMore) OffsetCursor(folderList.nextOffset!!) else null
                    val cacheMetadata = FolderCacheMetadata(params = params, nextCursor = nextCursor)
                    val cacheEntry = CacheEntry(cacheKey, timeProvider.currentInstant, metadataConverter.toJson(cacheMetadata))
                    val entities = folderList.folders.map { folderEntityMapper.map(it) }
                    persist(cacheEntry = cacheEntry, folders = entities, replaceExisting = offset == 0)
                }
                .ignoreElement()
    }

    private fun persist(cacheEntry: CacheEntry, folders: List<FolderEntity>, replaceExisting: Boolean) {
        database.runInTransaction {
            var order = if (replaceExisting) {
                collectionDao.deleteFoldersByKey(cacheEntry.key)
                1
            } else {
                collectionDao.maxOrder(cacheEntry.key) + 1
            }

            collectionDao.insertOrUpdateFolders(folders)
            cacheEntryRepository.setEntry(cacheEntry)

            val userFolders = folders.map { FolderLinkage(key = cacheEntry.key, folderId = it.id, order = order++) }
            collectionDao.insertLinks(userFolders)
        }
    }

    private fun getCacheKey(username: String): String {
        return "collection-folders-$username"
    }

    companion object {
        private val CACHE_DURATION = Duration.ofHours(2)
        private const val FOLDERS_PER_PAGE = 50
    }
}

@JsonClass(generateAdapter = true)
data class FolderCacheMetadata(
    @Json(name = "params")
    val params: FolderLoadParams,

    @Json(name = "next_cursor")
    val nextCursor: OffsetCursor?
)
