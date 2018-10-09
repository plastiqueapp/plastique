package io.plastique.gallery

import androidx.room.RoomDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.gallery.GalleryService
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
import io.reactivex.Single
import org.threeten.bp.Duration
import javax.inject.Inject

class FolderRepository @Inject constructor(
    private val database: RoomDatabase,
    private val galleryDao: GalleryDao,
    private val galleryService: GalleryService,
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
        return Single.fromCallable { params.username ?: sessionManager.currentUsername }
                .flatMapObservable { username ->
                    val cacheKey = getCacheKey(username)
                    cacheHelper.createObservable(
                            cacheKey = cacheKey,
                            cachedData = getFoldersFromDb(cacheKey),
                            updater = fetchFolders(params, cacheKey, null))
                }
    }

    fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor? = null): Completable {
        return Single.fromCallable { params.username ?: sessionManager.currentUsername }
                .flatMapCompletable { username -> fetchFolders(params, getCacheKey(username), cursor) }
    }

    private fun fetchFolders(params: FolderLoadParams, cacheKey: String, cursor: OffsetCursor?): Completable {
        val offset = cursor?.offset ?: 0
        return galleryService.getFolders(
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

    private fun getFoldersFromDb(cacheKey: String): Observable<PagedData<List<Folder>, OffsetCursor>> {
        return RxRoom.createObservable(database, arrayOf("gallery_folders", "user_gallery_folders")) {
            val folders = galleryDao.getFolders(cacheKey).map { folderMapper.map(it) }
            val nextCursor = getNextCursor(cacheKey)
            PagedData(folders, nextCursor)
        }
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<FolderCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    private fun persist(cacheEntry: CacheEntry, folders: List<FolderEntity>, replaceExisting: Boolean) {
        database.runInTransaction {
            var order = if (replaceExisting) {
                galleryDao.deleteFoldersByKey(cacheEntry.key)
                1
            } else {
                galleryDao.maxOrder(cacheEntry.key) + 1
            }

            galleryDao.insertOrUpdateFolders(folders)
            cacheEntryRepository.setEntry(cacheEntry)

            val userFolders = folders.map { FolderLinkage(key = cacheEntry.key, folderId = it.id, order = order++) }
            galleryDao.insertLinks(userFolders)
        }
    }

    private fun getCacheKey(username: String): String {
        return "gallery-folders-$username"
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
