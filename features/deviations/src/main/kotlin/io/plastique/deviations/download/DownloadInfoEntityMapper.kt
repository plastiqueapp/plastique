package io.plastique.deviations.download

import io.plastique.util.Size
import javax.inject.Inject

class DownloadInfoEntityMapper @Inject constructor() {
    fun map(entity: DownloadInfoEntity): DownloadInfo {
        return DownloadInfo(
                downloadUrl = entity.downloadUrl,
                size = Size.of(entity.width, entity.height),
                fileSize = entity.fileSize)
    }
}
