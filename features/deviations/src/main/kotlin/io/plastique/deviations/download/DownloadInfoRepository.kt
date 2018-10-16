package io.plastique.deviations.download

import io.plastique.api.deviations.DeviationService
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import io.plastique.api.deviations.DownloadInfo as DownloadInfoDto

class DownloadInfoRepository @Inject constructor(
    private val downloadInfoDao: DownloadInfoDao,
    private val deviationService: DeviationService
) {
    fun getDownloadInfo(deviationId: String): Single<DownloadInfo> {
        return getDownloadInfoFromDb(deviationId)
                .switchIfEmpty(getDownloadInfoFromServer(deviationId))
                .map { downloadInfoEntity -> downloadInfoEntity.toDownloadInfo() }
    }

    private fun getDownloadInfoFromDb(deviationId: String): Maybe<DownloadInfoEntity> {
        return downloadInfoDao.getDownloadInfo(deviationId)
    }

    private fun getDownloadInfoFromServer(deviationId: String): Single<DownloadInfoEntity> {
        return deviationService.getDeviationDownload(deviationId)
                .map { persist(deviationId, it) }
    }

    private fun persist(deviationId: String, downloadInfo: DownloadInfoDto): DownloadInfoEntity {
        val entity = downloadInfo.toDownloadInfoEntity(deviationId)
        downloadInfoDao.insertOrUpdate(entity)
        return entity
    }
}
