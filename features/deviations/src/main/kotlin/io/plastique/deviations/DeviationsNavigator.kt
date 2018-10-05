package io.plastique.deviations

import io.plastique.core.navigation.NavigationContext

interface DeviationsNavigator {
    fun openCommentsForDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openUserProfile(navigationContext: NavigationContext, username: String)
}
