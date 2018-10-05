package io.plastique.settings.about.licenses

sealed class LicensesItem {
    override fun toString(): String = javaClass.simpleName
}

object HeaderItem : LicensesItem()
data class LicenseItem(val license: License) : LicensesItem()
