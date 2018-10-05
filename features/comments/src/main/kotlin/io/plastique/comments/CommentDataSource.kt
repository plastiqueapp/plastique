package io.plastique.comments

import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.PagedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class CommentDataSource @Inject constructor(
    private val commentRepository: CommentRepository
) : PagedDataSource<List<Comment>, CommentTarget> {
    private val params = AtomicReference<CommentTarget>()
    private val nextCursor = AtomicReference<OffsetCursor>()

    override fun getData(params: CommentTarget): Observable<out PagedData<List<Comment>, *>> {
        this.params.set(params)
        return commentRepository.getComments(params)
                .doOnNext { data -> nextCursor.set(data.nextCursor) }
    }

    override fun loadMore(): Completable {
        return commentRepository.fetchComments(params.get(), nextCursor.get()!!)
    }

    override fun refresh(): Completable {
        return commentRepository.fetchComments(params.get())
    }
}
