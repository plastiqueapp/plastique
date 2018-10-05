package io.plastique.core.cache

import javax.inject.Inject

class CacheEntryRepository @Inject constructor(
    private val dao: CacheEntryDao
) {
    fun getEntryByKey(key: String): CacheEntry? {
        return dao.getEntryByKey(key)
    }

    fun setEntry(entry: CacheEntry) {
        dao.insertOrUpdate(entry)
    }

    fun deleteEntryByKey(key: String) {
        dao.deleteEntryByKey(key)
    }
}
