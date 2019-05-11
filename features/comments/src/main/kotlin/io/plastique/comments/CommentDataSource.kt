package io.plastique.comments

import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.PagedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class CommentDataSource @Inject constructor(
    private val commentRepository: CommentRepositoryImpl
) : PagedDataSource<List<Comment>, CommentThreadId> {
    private val params = AtomicReference<CommentThreadId>()
    private val nextCursor = AtomicReference<OffsetCursor>()

    override fun getData(params: CommentThreadId): Observable<out PagedData<List<Comment>, *>> {
        this.params.set(params)
        return commentRepository.getComments(params)
            .doOnNext { data -> nextCursor.set(data.nextCursor) }
    }

    override fun loadMore(): Completable {
        return commentRepository.fetchComments(params.get(), nextCursor.get()!!)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    override fun refresh(): Completable {
        return commentRepository.fetchComments(params.get())
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }
}
