package io.plastique.gallery.folders

import androidx.room.RoomDatabase
import com.github.technoir42.rxjava2.extensions.mapError
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.ApiException
import io.plastique.api.common.ErrorType
import io.plastique.api.gallery.FolderDto
import io.plastique.api.gallery.GalleryService
import io.plastique.api.nextCursor
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.CleanableRepository
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.db.createObservable
import io.plastique.core.json.adapters.NullFallbackAdapter
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.requireUser
import io.plastique.core.time.TimeProvider
import io.plastique.gallery.GalleryDao
import io.plastique.users.UserNotFoundException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.functions.Functions
import org.threeten.bp.Duration
import timber.log.Timber
import javax.inject.Inject

class GalleryFolderRepository @Inject constructor(
    private val database: RoomDatabase,
    private val galleryDao: GalleryDao,
    private val galleryService: GalleryService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val metadataConverter: NullFallbackAdapter,
    private val sessionManager: SessionManager,
    private val timeProvider: TimeProvider
) : CleanableRepository {

    fun getFolders(params: FolderLoadParams): Observable<PagedData<List<Folder>, OffsetCursor>> {
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(timeProvider, CACHE_DURATION) { serializedMetadata ->
            val metadata = metadataConverter.fromJson<FolderCacheMetadata>(serializedMetadata)
            metadata?.params == params
        }
        val cacheHelper = CacheHelper(cacheEntryRepository, cacheEntryChecker)
        return sessionManager.sessionChanges
            .firstOrError()
            .flatMapObservable { session ->
                val own = params.username == null || params.username == (session as? Session.User)?.username
                val cacheUsername = params.username ?: session.requireUser().username
                val cacheKey = getCacheKey(cacheUsername)
                cacheHelper.createObservable(
                    cacheKey = cacheKey,
                    cachedData = getFoldersFromDb(cacheKey, own),
                    updater = fetchFolders(params, null, cacheKey).ignoreElement())
            }
    }

    fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor? = null): Single<Optional<OffsetCursor>> {
        return sessionManager.sessionChanges
            .firstOrError()
            .flatMap { session ->
                val cacheUsername = params.username ?: session.requireUser().username
                fetchFolders(params, cursor, getCacheKey(cacheUsername))
            }
    }

    private fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor?, cacheKey: String): Single<Optional<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return galleryService.getFolders(
            username = params.username,
            matureContent = params.matureContent,
            preload = true,
            offset = offset,
            limit = FOLDERS_PER_PAGE)
            .map { folderList ->
                val cacheMetadata = FolderCacheMetadata(params = params, nextCursor = folderList.nextCursor)
                val cacheEntry = CacheEntry(cacheKey, timeProvider.currentInstant, metadataConverter.toJson(cacheMetadata))
                val entities = folderList.results.map { folder -> folder.toFolderEntity() }
                persist(cacheEntry = cacheEntry, folders = entities, replaceExisting = offset == 0)
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

    private fun getFoldersFromDb(cacheKey: String, own: Boolean): Observable<PagedData<List<Folder>, OffsetCursor>> {
        return database.createObservable("gallery_folders", "user_gallery_folders", "deleted_gallery_folders") {
            val folders = galleryDao.getFolders(cacheKey).asSequence()
                .map { it.toFolder(own) }
                .filter { own || it.isNotEmpty }
                .toList()
            val nextCursor = getNextCursor(cacheKey)
            PagedData(folders, nextCursor)
        }.distinctUntilChanged()
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<FolderCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    fun createFolder(folderName: String): Completable {
        return sessionManager.sessionChanges
            .firstOrError()
            .flatMap { session ->
                val username = session.requireUser().username
                galleryService.createFolder(folderName)
                    .doOnSuccess { folder -> persist(cacheKey = getCacheKey(username), folder = folder) }
            }
            .ignoreElement()
    }

    fun markAsDeleted(folderId: String, deleted: Boolean): Completable = Completable.fromAction {
        if (deleted) {
            galleryDao.insertDeletedFolder(DeletedFolderEntity(folderId = folderId))
        } else {
            galleryDao.removeDeletedFolder(folderId)
        }
    }

    fun deleteMarkedFolders(): Completable {
        return galleryDao.getDeletedFolderIds()
            .flattenAsObservable(Functions.identity())
            .concatMapCompletable { folderId ->
                galleryService.removeFolder(folderId)
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
                                galleryDao.deleteFolderById(folderId)
                            }
                            galleryDao.removeDeletedFolder(folderId)
                        }
                    }
                    .ignoreElement()
            }
    }

    override fun cleanCache(): Completable {
        return galleryDao.removeDeletedFolders()
    }

    private fun persist(cacheEntry: CacheEntry, folders: List<FolderEntity>, replaceExisting: Boolean) {
        database.runInTransaction {
            val startIndex = if (replaceExisting) {
                galleryDao.deleteFoldersByKey(cacheEntry.key)
                1
            } else {
                galleryDao.maxOrder(cacheEntry.key) + 1
            }

            galleryDao.insertOrUpdateFolders(folders)
            cacheEntryRepository.setEntry(cacheEntry)

            val userFolders = folders.mapIndexed { index, folder ->
                FolderLinkage(key = cacheEntry.key, folderId = folder.id, order = startIndex + index)
            }
            galleryDao.insertLinks(userFolders)
        }
    }

    private fun persist(cacheKey: String, folder: FolderDto) {
        database.runInTransaction {
            val order = galleryDao.maxOrder(cacheKey) + 1
            val link = FolderLinkage(key = cacheKey, folderId = folder.id, order = order)
            galleryDao.insertFolder(folder.toFolderEntity())
            galleryDao.insertLink(link)
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

private fun FolderDto.toFolderEntity(): FolderEntity {
    val thumbnailUrl = deviations.asSequence()
        .map { deviation -> deviation.thumbnails.lastOrNull()?.url ?: deviation.preview?.url }
        .firstOrNull { it != null }
    return FolderEntity(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
}

private fun FolderEntity.toFolder(own: Boolean): Folder = Folder(
    id = id,
    name = name,
    size = size,
    thumbnailUrl = thumbnailUrl,
    isDeletable = own && name != Folder.FEATURED)
