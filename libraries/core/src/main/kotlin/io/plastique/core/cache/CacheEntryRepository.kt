package io.plastique.core.cache

import javax.inject.Inject

@Suppress("NOTHING_TO_INLINE")
class CacheEntryRepository @Inject constructor(
    private val dao: CacheEntryDao
) {
    fun getEntryByKey(key: CacheKey): CacheEntry? {
        return dao.getEntryByKey(key.value)?.toCacheEntry()
    }

    fun setEntry(entry: CacheEntry) {
        dao.insertOrUpdate(entry.toCacheEntryEntity())
    }

    fun deleteEntryByKey(key: CacheKey) {
        dao.deleteEntryByKey(key.value)
    }

    private inline fun CacheEntryEntity.toCacheEntry(): CacheEntry =
        CacheEntry(key = key.toCacheKey(), timestamp = timestamp, metadata = metadata)

    private inline fun CacheEntry.toCacheEntryEntity(): CacheEntryEntity =
        CacheEntryEntity(key = key.value, timestamp = timestamp, metadata = metadata)
}
