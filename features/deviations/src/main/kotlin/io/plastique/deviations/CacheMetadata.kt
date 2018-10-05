package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.plastique.core.adapters.DateCursorAdapter
import io.plastique.core.adapters.DerivedClassAdapterFactory
import io.plastique.core.adapters.OffsetCursorAdapter
import io.plastique.core.adapters.StringEnumJsonAdapter
import io.plastique.core.paging.Cursor
import io.plastique.util.adapter

@JsonClass(generateAdapter = true)
data class DeviationCacheMetadata(
    @Json(name = "params")
    val params: FetchParams,

    @Json(name = "next_cursor")
    val nextCursor: Cursor?
)

class DeviationCacheMetadataSerializer(
    paramsType: Class<out FetchParams>,
    cursorType: Class<out Cursor>
) {
    private val moshi = Moshi.Builder()
            .add(OffsetCursorAdapter())
            .add(DateCursorAdapter())
            .add(StringEnumJsonAdapter.Factory())
            .add(DerivedClassAdapterFactory(FetchParams::class.java, paramsType))
            .add(DerivedClassAdapterFactory(Cursor::class.java, cursorType))
            .build()
    private val adapter
        get() = moshi.adapter<DeviationCacheMetadata>()

    fun deserialize(metadata: String): DeviationCacheMetadata? {
        return adapter.fromJson(metadata)
    }

    fun serialize(metadata: DeviationCacheMetadata): String {
        return adapter.toJson(metadata)
    }
}
