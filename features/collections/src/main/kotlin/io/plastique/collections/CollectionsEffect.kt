package io.plastique.collections

import com.sch.neon.Effect

sealed class CollectionsEffect : Effect() {
    data class LoadCollectionsEffect(val params: FolderLoadParams) : CollectionsEffect()
    object LoadMoreEffect : CollectionsEffect()
    object RefreshEffect : CollectionsEffect()
}
