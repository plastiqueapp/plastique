package io.plastique.deviations.download

import io.plastique.api.deviations.DownloadInfo
import javax.inject.Inject

class DownloadInfoMapper @Inject constructor() {
    fun map(deviationId: String, downloadInfo: DownloadInfo): DownloadInfoEntity {
        return DownloadInfoEntity(
                deviationId = deviationId,
                downloadUrl = downloadInfo.url,
                width = downloadInfo.width,
                height = downloadInfo.height,
                fileSize = downloadInfo.fileSize)
    }
}
