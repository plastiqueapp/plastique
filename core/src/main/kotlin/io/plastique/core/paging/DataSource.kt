package io.plastique.core.paging

import io.reactivex.Completable
import io.reactivex.Observable

data class PagedData<T : Any, out Cursor : Any>(val value: T, val nextCursor: Cursor?) {
    val hasMore: Boolean
        get() = nextCursor != null
}

interface PagedDataSource<T : Any, in Params : Any?> {
    fun getData(params: Params): Observable<out PagedData<T, Any>>

    fun loadMore(): Completable

    fun refresh(): Completable
}
