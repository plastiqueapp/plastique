package io.plastique.collections

import com.sch.neon.Effect

sealed class CollectionsEffect : Effect() {
    data class LoadCollectionsEffect(val params: FolderLoadParams) : CollectionsEffect()
    object LoadMoreEffect : CollectionsEffect()
    object RefreshEffect : CollectionsEffect()

    data class DeleteFolderEffect(val folderId: String, val folderName: String) : CollectionsEffect()
    data class UndoDeleteFolderEffect(val folderId: String) : CollectionsEffect()
}
