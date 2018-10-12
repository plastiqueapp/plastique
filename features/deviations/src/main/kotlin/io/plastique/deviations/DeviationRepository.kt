package io.plastique.deviations

import androidx.room.RoomDatabase
import io.plastique.api.deviations.DeviationMetadata
import io.plastique.api.deviations.DeviationService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.paging.Cursor
import io.plastique.core.paging.PagedData
import io.plastique.users.UserDao
import io.plastique.users.UserMapper
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
import java.util.concurrent.Callable
import javax.inject.Inject
import io.plastique.api.deviations.Deviation as DeviationDto

class DeviationRepository @Inject constructor(
    private val database: RoomDatabase,
    private val deviationDao: DeviationDao,
    private val deviationMetadataDao: DeviationMetadataDao,
    private val deviationService: DeviationService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val fetcherFactory: DeviationFetcherFactory,
    private val deviationMapper: DeviationMapper,
    private val deviationEntityMapper: DeviationEntityMapper,
    private val deviationMetadataMapper: DeviationMetadataMapper,
    private val timeProvider: TimeProvider,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) {
    fun getDeviations(params: FetchParams): Observable<PagedData<List<Deviation>, Cursor>> {
        val fetcher = fetcherFactory.createFetcher(params)
        val metadataSerializer = fetcher.createMetadataSerializer()
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(timeProvider, CACHE_DURATION) { serializedMetadata ->
            val metadata = metadataSerializer.deserialize(serializedMetadata)
            metadata?.params == params
        }
        val cacheKey = fetcher.getCacheKey(params)
        return CacheHelper(cacheEntryRepository, cacheEntryChecker).createObservable(
                cacheKey = cacheKey,
                cachedData = getDeviationsFromDb(cacheKey, params, metadataSerializer),
                updater = fetch(fetcher, cacheKey, params))
    }

    fun fetch(params: FetchParams, cursor: Cursor? = null): Completable {
        val fetcher = fetcherFactory.createFetcher(params)
        return fetch(fetcher, fetcher.getCacheKey(params), params, cursor)
    }

    private fun fetch(fetcher: DeviationFetcher<FetchParams, Cursor>, cacheKey: String, params: FetchParams, cursor: Cursor? = null): Completable {
        return fetcher.fetch(params, cursor)
                .doOnSuccess { fetchResult ->
                    val cacheMetadata = DeviationCacheMetadata(params, fetchResult.nextCursor)
                    val metadataSerializer = fetcher.createMetadataSerializer()
                    val cacheEntry = CacheEntry(cacheKey, timeProvider.currentInstant, metadataSerializer.serialize(cacheMetadata))
                    persist(cacheEntry, fetchResult.deviations, fetchResult.replaceExisting)
                }
                .ignoreElement()
    }

    private fun getDeviationsFromDb(key: String, params: FetchParams, metadataSerializer: DeviationCacheMetadataSerializer): Observable<PagedData<List<Deviation>, Cursor>> {
        return RxRoom.createObservable(database, arrayOf("deviations", "deviation_linkage")) {
            database.runInTransaction(Callable {
                val deviationsWithUsers = deviationDao.getDeviationsWithUsersByKey(key)
                val nextCursor = getNextCursor(key, metadataSerializer)
                val deviations = combineAndFilter(deviationsWithUsers, params)
                PagedData(deviations, nextCursor)
            })
        }.distinctUntilChanged()
    }

    private fun getNextCursor(cacheKey: String, metadataSerializer: DeviationCacheMetadataSerializer): Cursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataSerializer.deserialize(it) }
        return metadata?.nextCursor
    }

    private fun combineAndFilter(deviationsWithUsers: List<DeviationWithUsers>, params: FetchParams): List<Deviation> {
        return deviationsWithUsers
                .asSequence()
                .map { deviationEntityMapper.map(it) }
                .filter { deviation -> matchesParams(deviation, params) }
                .toList()
    }

    private fun matchesParams(deviation: Deviation, params: FetchParams): Boolean {
        return (!deviation.isLiterature || params.showLiterature) &&
                (!deviation.properties.isMature || params.showMatureContent)
    }

    private fun persist(cacheEntry: CacheEntry, deviations: List<DeviationDto>, replaceExisting: Boolean) {
        val entities = deviations.map { deviation -> deviationMapper.map(deviation) }
        val users = deviations.asSequence()
                .flatMap { deviation ->
                    val dailyDeviation = deviation.dailyDeviation
                    if (dailyDeviation != null) {
                        sequenceOf(deviation.author, dailyDeviation.giver)
                    } else {
                        sequenceOf(deviation.author)
                    }
                }
                .distinctBy { user -> user.id }
                .map { user -> userMapper.map(user) }
                .toList()

        database.runInTransaction {
            userDao.insertOrUpdate(users)
            deviationDao.insertOrUpdate(entities)
            cacheEntryRepository.setEntry(cacheEntry)

            var order = if (replaceExisting) {
                deviationDao.deleteLinksByKey(cacheEntry.key)
                1
            } else {
                deviationDao.getMaxOrder(cacheEntry.key) + 1
            }

            // TODO: Gracefully handle duplicates
            val links = deviations.map { deviation -> DeviationLinkage(cacheEntry.key, deviation.id, order++) }
            deviationDao.insertLinks(links)
        }
    }

    fun getDeviationById(deviationId: String): Observable<Deviation> {
        return getDeviationByIdFromDb(deviationId)
                .switchIfEmpty(getDeviationByIdFromServer(deviationId)
                        .ignoreElement()
                        .andThen(getDeviationByIdFromDb(deviationId)))
    }

    private fun getDeviationByIdFromDb(deviationId: String): Observable<Deviation> {
        return RxRoom.createObservable(database, arrayOf("deviations")) {
            deviationDao.getDeviationWithUsersById(deviationId)
        }
                .takeWhile { it.isNotEmpty() }
                .map { deviationEntityMapper.map(it.first()) }
    }

    private fun getDeviationByIdFromServer(deviationId: String): Single<DeviationDto> {
        return deviationService.getDeviationById(deviationId)
                .doOnSuccess { deviation -> persistDeviation(deviation) }
    }

    fun getDeviationTitleById(deviationId: String): Single<String> {
        return Maybe.fromCallable<String> { deviationDao.getDeviationTitleById(deviationId) }
                .switchIfEmpty(getDeviationByIdFromServer(deviationId)
                        .map { deviation -> deviation.title })
    }

    private fun getMetadataById(deviationId: String): Single<DeviationMetadata> {
        return deviationService.getMetadataByIds(listOf(deviationId))
                .doOnSuccess { response -> persist(response.metadata) }
                .flattenAsObservable { response -> response.metadata }
                .firstOrError()
    }

    private fun persistDeviation(deviation: DeviationDto) {
        val entity = deviationMapper.map(deviation)
        val author = userMapper.map(deviation.author)
        database.runInTransaction {
            userDao.insertOrUpdate(author)
            deviation.dailyDeviation?.run {
                userDao.insertOrUpdate(userMapper.map(giver))
            }
            deviationDao.insertOrUpdate(entity)
        }
    }

    private fun persist(metadata: List<DeviationMetadata>) {
        val entities = metadata.map { deviationMetadataMapper.map(it) }
        deviationMetadataDao.insertOrUpdate(entities)
    }

    companion object {
        private val CACHE_DURATION = Duration.ofHours(1)
    }
}
