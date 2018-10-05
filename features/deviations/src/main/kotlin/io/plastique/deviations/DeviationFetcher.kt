package io.plastique.deviations

import io.plastique.api.deviations.Deviation
import io.plastique.core.paging.Cursor
import io.plastique.deviations.categories.Category
import io.reactivex.Single
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Provider

interface FetchParams : Serializable {
    val showMatureContent: Boolean
    val showLiterature: Boolean

    fun isSameAs(params: FetchParams): Boolean

    fun with(showMatureContent: Boolean = this.showMatureContent, showLiterature: Boolean = this.showLiterature): FetchParams
}

data class FetchResult<TCursor : Cursor>(
    val deviations: List<Deviation>,
    val nextCursor: TCursor?,
    val replaceExisting: Boolean
)

interface DeviationFetcher<TParams : FetchParams, TCursor : Cursor> {
    fun getCacheKey(params: TParams): String

    fun createMetadataSerializer(): DeviationCacheMetadataSerializer

    fun fetch(params: TParams, cursor: TCursor?): Single<FetchResult<TCursor>>

    val Category.pathOrNull: String?
        get() = if (path != Category.ALL.path) path else null
}

class DeviationFetcherFactory @Inject constructor(
    private val factories: Map<Class<*>, @JvmSuppressWildcards Provider<DeviationFetcher<out FetchParams, out Cursor>>>
) {
    fun createFetcher(params: FetchParams): DeviationFetcher<FetchParams, Cursor> {
        @Suppress("UNCHECKED_CAST")
        return factories.getValue(params.javaClass).get() as DeviationFetcher<FetchParams, Cursor>
    }
}
