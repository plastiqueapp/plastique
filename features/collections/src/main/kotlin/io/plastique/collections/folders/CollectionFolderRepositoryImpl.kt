package io.plastique.collections.folders

import androidx.room.RoomDatabase
import com.github.technoir42.rxjava2.extensions.mapError
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.ApiException
import io.plastique.api.collections.CollectionService
import io.plastique.api.collections.FolderDto
import io.plastique.api.common.ErrorType
import io.plastique.api.nextCursor
import io.plastique.collections.CollectionDao
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.CacheKey
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.cache.toCacheKey
import io.plastique.core.db.createObservable
import io.plastique.core.json.adapters.NullFallbackAdapter
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.requireUser
import io.plastique.users.UserNotFoundException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.functions.Functions
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import timber.log.Timber
import javax.inject.Inject

class CollectionFolderRepositoryImpl @Inject constructor(
    private val clock: Clock,
    private val database: RoomDatabase,
    private val collectionDao: CollectionDao,
    private val collectionService: CollectionService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val metadataConverter: NullFallbackAdapter,
    private val sessionManager: SessionManager
) : CollectionFolderRepository {

    fun getFolders(params: FolderLoadParams): Observable<PagedData<List<Folder>, OffsetCursor>> {
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(clock, CACHE_DURATION) { serializedMetadata ->
            val metadata = metadataConverter.fromJson<FolderCacheMetadata>(serializedMetadata)
            metadata?.params == params
        }
        val cacheHelper = CacheHelper(cacheEntryRepository, cacheEntryChecker)
        return sessionManager.sessionChanges
            .firstOrError()
            .flatMapObservable { session ->
                val own = params.username == null || params.username == (session as? Session.User)?.username
                val owner = params.username ?: session.requireUser().username
                val cacheKey = getCacheKey(owner)
                cacheHelper.createObservable(
                    cacheKey = cacheKey,
                    cachedData = getFoldersFromDb(cacheKey, owner, own),
                    updater = fetchFolders(params, cacheKey, cursor = null).ignoreElement())
            }
    }

    private fun getFoldersFromDb(cacheKey: CacheKey, owner: String, own: Boolean): Observable<PagedData<List<Folder>, OffsetCursor>> {
        return database.createObservable("collection_folders", "user_collection_folders", "deleted_collection_folders") {
            val folders = collectionDao.getFoldersByKey(cacheKey.value).asSequence()
                .map { it.toFolder(owner, own) }
                .filter { own || it.isNotEmpty }
                .toList()
            val nextCursor = getNextCursor(cacheKey)
            PagedData(folders, nextCursor)
        }.distinctUntilChanged()
    }

    private fun getNextCursor(cacheKey: CacheKey): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<FolderCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor? = null): Single<Optional<OffsetCursor>> {
        return sessionManager.sessionChanges
            .firstOrError()
            .flatMap { session ->
                val cacheUsername = params.username ?: session.requireUser().username
                fetchFolders(params, getCacheKey(cacheUsername), cursor)
            }
    }

    private fun fetchFolders(params: FolderLoadParams, cacheKey: CacheKey, cursor: OffsetCursor?): Single<Optional<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return collectionService.getFolders(
            username = params.username,
            matureContent = params.matureContent,
            preload = true,
            offset = offset,
            limit = FOLDERS_PER_PAGE)
            .map { folderList ->
                val cacheMetadata = FolderCacheMetadata(params = params, nextCursor = folderList.nextCursor)
                val cacheEntry = CacheEntry(key = cacheKey, timestamp = clock.instant(), metadata = metadataConverter.toJson(cacheMetadata))
                persist(cacheEntry = cacheEntry, folders = folderList.results, replaceExisting = offset == 0)
                cacheMetadata.nextCursor.toOptional()
            }
            .mapError { error ->
                if (params.username != null && error is ApiException && error.errorData.type == ErrorType.InvalidRequest) {
                    UserNotFoundException(params.username, error)
                } else {
                    error
                }
            }
    }

    override fun put(folders: Collection<FolderDto>) {
        if (folders.isEmpty()) {
            return
        }
        val entities = folders.map { it.toFolderEntity() }
        collectionDao.insertOrUpdateFolders(entities)
    }

    override fun createFolder(folderName: String): Completable {
        return sessionManager.sessionChanges
            .firstOrError()
            .flatMap { session ->
                val username = session.requireUser().username
                collectionService.createFolder(folderName)
                    .doOnSuccess { folder -> persist(cacheKey = getCacheKey(username), folder = folder) }
            }
            .ignoreElement()
    }

    override fun markAsDeleted(folderId: String, deleted: Boolean): Completable = Completable.fromAction {
        if (deleted) {
            collectionDao.insertDeletedFolder(DeletedFolderEntity(folderId = folderId))
        } else {
            collectionDao.removeDeletedFolder(folderId)
        }
    }

    override fun deleteMarkedFolders(): Completable {
        return collectionDao.getDeletedFolderIds()
            .flattenAsObservable(Functions.identity())
            .concatMapCompletable { folderId ->
                collectionService.removeFolder(folderId)
                    .toSingleDefault(true)
                    .onErrorResumeNext { error ->
                        if (error is ApiException) {
                            Timber.e(error)
                            Single.just(false)
                        } else {
                            Single.error(error)
                        }
                    }
                    .doOnSuccess { wasDeleted ->
                        database.runInTransaction {
                            if (wasDeleted) {
                                collectionDao.deleteFolderById(folderId)
                            }
                            collectionDao.removeDeletedFolder(folderId)
                        }
                    }
                    .ignoreElement()
            }
    }

    override fun cleanCache(): Completable {
        return collectionDao.removeDeletedFolders()
    }

    private fun persist(cacheEntry: CacheEntry, folders: List<FolderDto>, replaceExisting: Boolean) {
        database.runInTransaction {
            val startIndex = if (replaceExisting) {
                collectionDao.deleteFoldersByKey(cacheEntry.key.value)
                1
            } else {
                collectionDao.maxOrder(cacheEntry.key.value) + 1
            }

            put(folders)
            cacheEntryRepository.setEntry(cacheEntry)

            val userFolders = folders.mapIndexed { index, folder ->
                FolderLinkage(key = cacheEntry.key.value, folderId = folder.id, order = startIndex + index)
            }
            collectionDao.insertLinks(userFolders)
        }
    }

    private fun persist(cacheKey: CacheKey, folder: FolderDto) {
        database.runInTransaction {
            val order = collectionDao.maxOrder(cacheKey.value) + 1
            val link = FolderLinkage(key = cacheKey.value, folderId = folder.id, order = order)
            collectionDao.insertFolder(folder.toFolderEntity())
            collectionDao.insertLink(link)
        }
    }

    private fun getCacheKey(username: String): CacheKey =
        "collection-folders-$username".toCacheKey()

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

private fun FolderDto.toFolderEntity(): FolderEntity {
    val thumbnailUrl = deviations.asSequence()
        .map { deviation -> deviation.thumbnails.lastOrNull()?.url ?: deviation.preview?.url }
        .firstOrNull { it != null }
    return FolderEntity(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
}
