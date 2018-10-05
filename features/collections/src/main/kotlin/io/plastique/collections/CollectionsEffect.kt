package io.plastique.collections

import io.plastique.core.flow.Effect

sealed class CollectionsEffect : Effect() {
    data class LoadCollectionsEffect(val params: FolderLoadParams) : CollectionsEffect()

    object LoadMoreEffect : CollectionsEffect()
    object RefreshEffect : CollectionsEffect()
}
