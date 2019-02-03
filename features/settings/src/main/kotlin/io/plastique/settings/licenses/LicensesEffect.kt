package io.plastique.settings.licenses

import io.plastique.core.flow.Effect

sealed class LicensesEffect : Effect() {
    object LoadLicensesEffect : LicensesEffect()
}
