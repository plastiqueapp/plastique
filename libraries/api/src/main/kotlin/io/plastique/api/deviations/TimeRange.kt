package io.plastique.api.deviations

import io.plastique.api.common.StringEnum

enum class TimeRange(override val value: String) : StringEnum {
    Hours8("8hr"),
    Hours24("24hr"),
    Days3("3days"),
    Week("1week"),
    Month("1month"),
    AllTime("alltime")
}
