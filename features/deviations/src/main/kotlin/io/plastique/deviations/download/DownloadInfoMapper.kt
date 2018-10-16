package io.plastique.deviations.download

import io.plastique.api.deviations.DownloadInfo
import io.plastique.util.Size
import javax.inject.Inject

class DownloadInfoMapper @Inject constructor() {
    fun map(deviationId: String, downloadInfo: DownloadInfo): DownloadInfoEntity {
        return DownloadInfoEntity(
                deviationId = deviationId,
                downloadUrl = downloadInfo.url,
                size = Size.of(downloadInfo.width, downloadInfo.height),
                fileSize = downloadInfo.fileSize)
    }
}
