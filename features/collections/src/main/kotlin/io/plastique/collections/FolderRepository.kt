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
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.users.UserRepository
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
    private val userRepository: UserRepository,
    private val metadataConverter: NullFallbackConverter,
    private val sessionManager: SessionManager,
    private val timeProvider: TimeProvider
) {

    private fun getUserIdByUsername(username: String?): Single<String> {
        return Single.defer {
            if (username == null) {
                val session = sessionManager.session
                // TODO: UserNotAuthenticatedException or something, depending on how normal this outcome is
                val userId = if (session is Session.User) session.userId else throw IllegalStateException("User is not authenticated")
                Single.just(userId)
            } else {
                userRepository.getUserByName(username)
                        .map { user -> user.id }
            }
        }
    }

    fun getFolders(params: FolderLoadParams): Observable<PagedData<List<Folder>, OffsetCursor>> {
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(timeProvider, CACHE_DURATION) { serializedMetadata ->
            val metadata = metadataConverter.fromJson<FolderCacheMetadata>(serializedMetadata)
            metadata?.params == params
        }
        val cacheHelper = CacheHelper(cacheEntryRepository, cacheEntryChecker)
        return getUserIdByUsername(params.username)
                .flatMapObservable { userId ->
                    val cacheKey = getCacheKey(userId)
                    cacheHelper.createObservable(
                            cacheKey = cacheKey,
                            cachedData = getFoldersFromDb(userId),
                            updater = getFoldersFromServer(params, userId))
                }
    }

    private fun getFoldersFromDb(userId: String): Observable<PagedData<List<Folder>, OffsetCursor>> {
        // TODO: Consider using cache key instead
        return RxRoom.createObservable(database, arrayOf("collection_folders", "user_collection_folders")) {
            val folders = collectionDao.getFolders(userId).map { folderMapper.map(it) }
            val nextCursor = getNextCursor(userId)
            PagedData(folders, nextCursor)
        }
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<FolderCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    fun getFoldersFromServer(params: FolderLoadParams, cursor: OffsetCursor? = null): Completable {
        // TODO: Get rid of this
        return getUserIdByUsername(params.username)
                .flatMapCompletable { userId -> getFoldersFromServer(params, userId, cursor) }
    }

    private fun getFoldersFromServer(params: FolderLoadParams, userId: String, cursor: OffsetCursor? = null): Completable {
        val offset = cursor?.offset ?: 0
        val cacheKey = getCacheKey(userId)

        return collectionService.getFolders(username = params.username, preload = true, matureContent = params.matureContent, offset = offset, limit = FOLDERS_PER_PAGE)
                .map { folderList ->
                    val nextCursor = if (folderList.hasMore) OffsetCursor(folderList.nextOffset!!) else null
                    val cacheMetadata = FolderCacheMetadata(params = params, nextCursor = nextCursor)
                    val cacheEntry = CacheEntry(cacheKey, timeProvider.currentInstant, metadataConverter.toJson(cacheMetadata))
                    val entities = folderList.folders.map { folderEntityMapper.map(it) }
                    persist(userId = userId, folders = entities, cacheEntry = cacheEntry, replaceExisting = offset == 0)
                }
                .ignoreElement()
    }

    private fun persist(userId: String, folders: List<FolderEntity>, cacheEntry: CacheEntry, replaceExisting: Boolean) {
        database.runInTransaction {
            var order = if (replaceExisting) {
                collectionDao.deleteFoldersByUserId(userId)
                1
            } else {
                collectionDao.maxOrder(userId) + 1
            }

            collectionDao.insertOrUpdateFolders(folders)
            cacheEntryRepository.setEntry(cacheEntry)

            val userFolders = folders.map { FolderLinkage(userId = userId, folderId = it.id, order = order++) }
            collectionDao.insertLinks(userFolders) // TODO: Make sure the user is in cache
        }
    }

    private fun getCacheKey(userId: String): String {
        return "collection-folders-$userId"
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
