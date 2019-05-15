package io.plastique.core.cache

import io.reactivex.Completable

interface CleanableRepository {
    fun cleanCache(): Completable
}
