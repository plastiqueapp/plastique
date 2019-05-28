package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.plastique.core.json.DateCursorAdapter
import io.plastique.core.json.DerivedClassAdapterFactory
import io.plastique.core.json.OffsetCursorAdapter
import io.plastique.core.json.StringEnumJsonAdapter
import io.plastique.core.json.adapter
import io.plastique.core.paging.Cursor

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

    fun deserialize(metadata: String): DeviationCacheMetadata? = try {
        adapter.fromJson(metadata)
    } catch (e: JsonDataException) {
        null
    }

    fun serialize(metadata: DeviationCacheMetadata): String {
        return adapter.toJson(metadata)
    }
}
