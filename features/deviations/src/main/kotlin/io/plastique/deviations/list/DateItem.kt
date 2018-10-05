package io.plastique.deviations.list

import io.plastique.core.lists.ListItem
import org.threeten.bp.LocalDate

data class DateItem(val date: LocalDate) : ListItem {
    override val id: String = date.toString()
}
