package io.plastique.api.gallery

import io.plastique.api.common.StringEnum

enum class SortOrder(override val value: String) : StringEnum {
    Newest("newest"),
    Popular("popular")
}
