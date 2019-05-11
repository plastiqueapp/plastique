package io.plastique.deviations.download

import io.plastique.api.deviations.DeviationService
import io.plastique.api.deviations.DownloadInfoDto
import io.plastique.util.Size
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

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
        return deviationService.getDownloadInfoById(deviationId)
            .map { persist(deviationId, it) }
    }

    private fun persist(deviationId: String, downloadInfo: DownloadInfoDto): DownloadInfoEntity {
        val entity = downloadInfo.toDownloadInfoEntity(deviationId)
        downloadInfoDao.insertOrUpdate(entity)
        return entity
    }
}

private fun DownloadInfoDto.toDownloadInfoEntity(deviationId: String): DownloadInfoEntity = DownloadInfoEntity(
    deviationId = deviationId,
    downloadUrl = url,
    size = Size(width, height),
    fileSize = fileSize)

private fun DownloadInfoEntity.toDownloadInfo(): DownloadInfo =
    DownloadInfo(downloadUrl = downloadUrl, size = size, fileSize = fileSize)
