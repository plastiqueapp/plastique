package io.plastique.inject.modules

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.plastique.core.browser.CookieCleaner
import io.plastique.core.cache.CacheCleaner
import io.plastique.core.session.OnLogoutListener
import io.plastique.core.session.onLogoutListener
import io.plastique.core.work.WorkerCleaner

@Module
object SessionModule {
    @Provides
    @IntoSet
    fun provideCacheCleanerListener(cacheCleaner: CacheCleaner): OnLogoutListener = onLogoutListener(cacheCleaner::clean)

    @Provides
    @IntoSet
    fun provideCookieCleanerListener(cookieCleaner: CookieCleaner): OnLogoutListener = onLogoutListener(cookieCleaner::clean)

    @Provides
    @IntoSet
    fun provideWorkerCleanerListener(workerCleaner: WorkerCleaner): OnLogoutListener = onLogoutListener(workerCleaner::clean)
}
