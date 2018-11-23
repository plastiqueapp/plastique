package io.plastique.settings.about.licenses

import io.plastique.core.lists.ListItem

object HeaderItem : ListItem {
    override val id: String get() = "header"
}

data class LicenseItem(val license: License) : ListItem {
    override val id: String get() = license.libraryName
}
