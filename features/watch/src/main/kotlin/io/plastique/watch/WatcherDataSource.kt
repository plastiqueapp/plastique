package io.plastique.watch

import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.PagedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class WatcherDataSource @Inject constructor(
    private val watcherRepository: WatcherRepository
) : PagedDataSource<List<Watcher>, String?> {
    private val params = AtomicReference<String>()
    private val nextCursor = AtomicReference<OffsetCursor>()

    override fun getData(params: String?): Observable<out PagedData<List<Watcher>, *>> {
        this.params.set(params)
        return watcherRepository.getWatchers(params)
                .doOnNext { data -> nextCursor.set(data.nextCursor) }
    }

    override fun loadMore(): Completable {
        return watcherRepository.fetchWatchers(params.get(), nextCursor.get()!!)
    }

    override fun refresh(): Completable {
        return watcherRepository.fetchWatchers(params.get())
    }
}
