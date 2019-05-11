package io.plastique.gallery

import androidx.room.RoomDatabase
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.sch.rxjava2.extensions.mapError
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.ErrorType
import io.plastique.api.gallery.FolderDto
import io.plastique.api.gallery.GalleryService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.UserNotFoundException
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.nextCursor
import io.plastique.core.session.SessionManager
import io.plastique.core.session.currentUsername
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
import javax.inject.Inject

class GalleryFolderRepository @Inject constructor(
    private val database: RoomDatabase,
    private val galleryDao: GalleryDao,
    private val galleryService: GalleryService,
    private val cacheEntryRepository: CacheEntryRepository,
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
                updater = fetchFolders(params, null, cacheKey).ignoreElement())
        }
    }

    fun fetchFolders(params: FolderLoadParams, cursor: OffsetCursor? = null): Single<Optional<OffsetCursor>> {
        return Single.defer {
            val cacheKey = getCacheKey(params.username ?: sessionManager.currentUsername)
            fetchFolders(params, cursor, cacheKey)
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

    private fun getFoldersFromDb(cacheKey: String): Observable<PagedData<List<Folder>, OffsetCursor>> {
        return RxRoom.createObservable(database, arrayOf("gallery_folders", "user_gallery_folders")) {
            val folders = galleryDao.getFolders(cacheKey).map { it.toFolder() }
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

private fun FolderDto.toFolderEntity(): FolderEntity {
    val thumbnailUrl = deviations.asSequence()
        .map { deviation -> deviation.thumbnails.lastOrNull()?.url ?: deviation.preview?.url }
        .firstOrNull { it != null }
    return FolderEntity(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
}

private fun FolderEntity.toFolder(): Folder =
    Folder(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
