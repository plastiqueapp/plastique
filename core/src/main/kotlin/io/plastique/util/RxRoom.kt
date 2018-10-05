package io.plastique.util

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers

object RxRoom {
    fun <T : Any> createObservable(database: RoomDatabase, tableNames: Array<String>, callable: () -> T): Observable<T> {
        return createObservable(database, tableNames)
                .observeOn(Schedulers.io())
                .map { callable().toOptional() }
                .filter { optional -> optional.isPresent }
                .map { optional -> optional.value }
    }

    private fun createObservable(database: RoomDatabase, tableNames: Array<String>): Observable<Any> {
        return Observable.create { emitter ->
            val observer = object : InvalidationTracker.Observer(tableNames) {
                override fun onInvalidated(tables: Set<String>) {
                    emitter.onNext(Unit)
                }
            }

            database.invalidationTracker.addObserver(observer)
            emitter.setDisposable(Disposables.fromAction { database.invalidationTracker.removeObserver(observer) })
            emitter.onNext(Unit)
        }
    }
}
