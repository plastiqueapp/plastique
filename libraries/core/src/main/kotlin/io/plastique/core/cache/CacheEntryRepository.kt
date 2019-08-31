package io.plastique.core.cache

import javax.inject.Inject

class CacheEntryRepository @Inject constructor(
    private val dao: CacheEntryDao
) {
    fun getEntryByKey(key: String): CacheEntry? {
        return dao.getEntryByKey(key)?.toCacheEntry()
    }

    fun setEntry(entry: CacheEntry) {
        dao.insertOrUpdate(entry.toCacheEntryEntity())
    }

    fun deleteEntryByKey(key: String) {
        dao.deleteEntryByKey(key)
    }

    private fun CacheEntryEntity.toCacheEntry(): CacheEntry =
        CacheEntry(key = key, timestamp = timestamp, metadata = metadata)

    private fun CacheEntry.toCacheEntryEntity(): CacheEntryEntity =
        CacheEntryEntity(key = key, timestamp = timestamp, metadata = metadata)
}
