package io.plastique.core.work

import io.reactivex.subjects.SingleSubject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.util.concurrent.ExecutionException

class RxWorkerTest {
    @Test
    fun toListenableFuture_onSuccess() {
        val source = SingleSubject.create<Int>()
        val future = source.toListenableFuture()

        assertFalse(future.isDone)

        source.onSuccess(1)

        assertTrue(future.isDone)
        assertFalse(source.hasObservers())
        assertEquals(1, future.get())
    }

    @Test
    fun toListenableFuture_onError() {
        val source = SingleSubject.create<Any>()
        val future = source.toListenableFuture()

        val error = IOException()
        source.onError(error)

        assertTrue(future.isDone)
        assertFalse(source.hasObservers())

        val e = assertThrows<ExecutionException> { future.get() }
        assertSame(error, e.cause)
    }

    @Test
    fun toListenableFuture_cancel() {
        val source = SingleSubject.create<Int>()
        val future = source.toListenableFuture()

        assertFalse(future.isCancelled)

        future.cancel(true)

        assertTrue(future.isCancelled)
        assertFalse(source.hasObservers())
    }
}
