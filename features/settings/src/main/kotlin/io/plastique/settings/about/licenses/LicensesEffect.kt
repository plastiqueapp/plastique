package io.plastique.settings.about.licenses

import io.plastique.core.flow.Effect

sealed class LicensesEffect : Effect() {
    object LoadLicensesEffect : LicensesEffect()
}
