package io.plastique.deviations.download

import javax.inject.Inject

class DownloadInfoEntityMapper @Inject constructor() {
    fun map(entity: DownloadInfoEntity): DownloadInfo {
        return DownloadInfo(
                downloadUrl = entity.downloadUrl,
                size = entity.size,
                fileSize = entity.fileSize)
    }
}
