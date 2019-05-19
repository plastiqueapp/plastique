package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.plastique.collections.CollectionFolderRepository
import io.plastique.core.cache.CacheCleaner
import io.plastique.core.cache.CleanableRepository
import io.plastique.core.session.OnLogoutListener
import io.plastique.feed.FeedRepository
import io.plastique.gallery.GalleryFolderRepository
import io.plastique.notifications.MessageRepository

@Module
interface CacheModule {
    @Binds
    @IntoSet
    fun bindCacheCleaner(impl: CacheCleaner): OnLogoutListener

    @Binds
    @IntoSet
    fun bindCollectionFolderRepository(impl: CollectionFolderRepository): CleanableRepository

    @Binds
    @IntoSet
    fun bindFeedRepository(impl: FeedRepository): CleanableRepository

    @Binds
    @IntoSet
    fun bindGalleryFolderRepository(impl: GalleryFolderRepository): CleanableRepository

    @Binds
    @IntoSet
    fun bindMessageRepository(impl: MessageRepository): CleanableRepository
}
