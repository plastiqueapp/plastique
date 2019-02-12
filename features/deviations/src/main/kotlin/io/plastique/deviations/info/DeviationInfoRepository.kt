package io.plastique.deviations.info

import androidx.room.RoomDatabase
import com.sch.rxjava2.extensions.mapError
import io.plastique.api.common.ErrorType
import io.plastique.api.deviations.DeviationMetadataDto
import io.plastique.api.deviations.DeviationService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.exceptions.ApiException
import io.plastique.deviations.DeviationNotFoundException
import io.plastique.deviations.DeviationRepository
import io.plastique.users.toUser
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Singles
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import javax.inject.Inject

class DeviationInfoRepository @Inject constructor(
    private val database: RoomDatabase,
    private val deviationService: DeviationService,
    private val deviationMetadataDao: DeviationMetadataDao,
    private val cacheEntryRepository: CacheEntryRepository,
    private val timeProvider: TimeProvider,
    private val deviationRepository: DeviationRepository
) {
    private val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))

    fun getDeviationInfo(deviationId: String): Observable<DeviationInfo> {
        val cacheKey = "deviation-info-$deviationId"
        return cacheHelper.createObservable(
                cacheKey = cacheKey,
                cachedData = getDeviationInfoFromDb(deviationId),
                updater = fetch(deviationId, cacheKey))
    }

    private fun fetch(deviationId: String, cacheKey: String): Completable {
        return Singles.zip(
                deviationRepository.getDeviationTitleById(deviationId),
                deviationService.getMetadataByIds(listOf(deviationId))
                        .mapError { error ->
                            if (error is ApiException && error.errorData.type == ErrorType.InvalidRequest && error.errorData.details.containsKey("deviationids")) {
                                DeviationNotFoundException(deviationId, error)
                            } else {
                                error
                            }
                        }) { _, metadataResult -> persist(cacheKey, metadataResult.metadata) }
                .ignoreElement()
    }

    private fun getDeviationInfoFromDb(deviationId: String): Observable<DeviationInfo> {
        return deviationMetadataDao.getDeviationInfoById(deviationId)
                .filter { it.isNotEmpty() }
                .map { it.first().toDeviationInfo(ZoneId.systemDefault()) }
    }

    private fun persist(cacheKey: String, metadataList: List<DeviationMetadataDto>) {
        val entities = metadataList.map { it.toDeviationMetadataEntity() }
        database.runInTransaction {
            val cacheEntry = CacheEntry(key = cacheKey, timestamp = timeProvider.currentInstant)
            cacheEntryRepository.setEntry(cacheEntry)
            deviationMetadataDao.insertOrUpdate(entities)
        }
    }

    companion object {
        private val CACHE_DURATION = Duration.ofHours(2)
    }
}

private fun DeviationInfoEntity.toDeviationInfo(zoneId: ZoneId): DeviationInfo = DeviationInfo(
        title = title,
        author = users.first().toUser(),
        publishTime = publishTime.atZone(zoneId),
        description = description,
        tags = tags)

private fun DeviationMetadataDto.toDeviationMetadataEntity(): DeviationMetadataEntity = DeviationMetadataEntity(
        deviationId = deviationId,
        description = description,
        tags = tags.map { it.name })
