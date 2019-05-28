package io.plastique.api

import io.plastique.api.common.PagedListResult
import io.plastique.core.paging.OffsetCursor

val PagedListResult<*>.nextCursor: OffsetCursor?
    get() = if (hasMore) OffsetCursor(nextOffset!!) else null
