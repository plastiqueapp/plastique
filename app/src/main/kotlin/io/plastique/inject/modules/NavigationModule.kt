package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import io.plastique.AppNavigator
import io.plastique.auth.AuthNavigator
import io.plastique.collections.CollectionsNavigator
import io.plastique.comments.CommentsNavigator
import io.plastique.deviations.DeviationsNavigator
import io.plastique.feed.FeedNavigator
import io.plastique.gallery.GalleryNavigator
import io.plastique.main.MainNavigator
import io.plastique.notifications.NotificationsNavigator
import io.plastique.profile.ProfileNavigator
import io.plastique.settings.SettingsNavigator
import io.plastique.statuses.StatusesNavigator
import io.plastique.users.UsersNavigator
import io.plastique.watch.WatchNavigator

@Module
interface NavigationModule {
    @Binds
    fun bindAuthNavigator(impl: AppNavigator): AuthNavigator

    @Binds
    fun bindCollectionsNavigator(impl: AppNavigator): CollectionsNavigator

    @Binds
    fun bindCommentsNavigator(impl: AppNavigator): CommentsNavigator

    @Binds
    fun bindDeviationsNavigator(impl: AppNavigator): DeviationsNavigator

    @Binds
    fun bindFeedNavigator(impl: AppNavigator): FeedNavigator

    @Binds
    fun bindGalleryNavigator(impl: AppNavigator): GalleryNavigator

    @Binds
    fun bindMainNavigator(impl: AppNavigator): MainNavigator

    @Binds
    fun bindNotificationsNavigator(impl: AppNavigator): NotificationsNavigator

    @Binds
    fun bindProfileNavigator(impl: AppNavigator): ProfileNavigator

    @Binds
    fun bindSettingsNavigator(impl: AppNavigator): SettingsNavigator

    @Binds
    fun bindStatusesNavigator(impl: AppNavigator): StatusesNavigator

    @Binds
    fun bindUsersNavigator(impl: AppNavigator): UsersNavigator

    @Binds
    fun bindWatchNavigator(impl: AppNavigator): WatchNavigator
}
