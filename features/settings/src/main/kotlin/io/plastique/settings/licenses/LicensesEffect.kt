package io.plastique.settings.licenses

import com.sch.neon.Effect

sealed class LicensesEffect : Effect() {
    object LoadLicensesEffect : LicensesEffect()
}
