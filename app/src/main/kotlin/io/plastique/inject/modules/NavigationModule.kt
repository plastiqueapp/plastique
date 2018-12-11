package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import io.plastique.Navigator
import io.plastique.collections.CollectionsNavigator
import io.plastique.comments.CommentsNavigator
import io.plastique.deviations.DeviationsNavigator
import io.plastique.feed.FeedNavigator
import io.plastique.gallery.GalleryNavigator
import io.plastique.main.MainNavigator
import io.plastique.profile.ProfileNavigator
import io.plastique.settings.SettingsNavigator
import io.plastique.users.UsersNavigator
import io.plastique.watch.WatchNavigator

@Module
interface NavigationModule {
    @Binds
    fun bindCollectionsNavigator(impl: Navigator): CollectionsNavigator

    @Binds
    fun bindCommentsNavigator(impl: Navigator): CommentsNavigator

    @Binds
    fun bindDeviationsNavigator(impl: Navigator): DeviationsNavigator

    @Binds
    fun bindFeedNavigator(impl: Navigator): FeedNavigator

    @Binds
    fun bindGalleryNavigator(impl: Navigator): GalleryNavigator

    @Binds
    fun bindMainNavigator(impl: Navigator): MainNavigator

    @Binds
    fun bindProfileNavigator(impl: Navigator): ProfileNavigator

    @Binds
    fun bindSettingsNavigator(impl: Navigator): SettingsNavigator

    @Binds
    fun bindUsersNavigator(impl: Navigator): UsersNavigator

    @Binds
    fun bindWatchNavigator(impl: Navigator): WatchNavigator
}
