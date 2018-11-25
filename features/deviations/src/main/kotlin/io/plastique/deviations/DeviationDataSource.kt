package io.plastique.deviations

import io.plastique.core.paging.Cursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.PagedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class DeviationDataSource @Inject constructor(
    private val deviationRepository: DeviationRepositoryImpl
) : PagedDataSource<List<Deviation>, FetchParams> {
    private val params = AtomicReference<FetchParams>()
    private val nextCursor = AtomicReference<Cursor>()

    override fun getData(params: FetchParams): Observable<out PagedData<List<Deviation>, *>> {
        this.params.set(params)
        return deviationRepository.getDeviations(params)
                .doOnNext { data -> nextCursor.set(data.nextCursor) }
    }

    override fun loadMore(): Completable {
        val params = params.get()
        return if (params != null) {
            deviationRepository.fetch(params, nextCursor.get())
                    .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                    .ignoreElement()
        } else {
            Completable.complete()
        }
    }

    override fun refresh(): Completable {
        val params = params.get()
        return if (params != null) {
            deviationRepository.fetch(params)
                    .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                    .ignoreElement()
        } else {
            Completable.complete()
        }
    }
}
